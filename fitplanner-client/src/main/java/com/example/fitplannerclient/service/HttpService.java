package com.example.fitplannerclient.service;

import com.example.fitplannerclient.exception.RequestException;
import com.example.fitplannercommon.ErrorResponseDTO;
import com.example.fitplannercommon.TokenDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class HttpService {
    private static final Logger logger = LoggerFactory.getLogger(HttpService.class);
    private static final String CONTENT_TYPE = "application/json";

    private final String baseUrl;
    private final TokenStore tokenStore;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final Supplier<CompletableFuture<Boolean>> onSessionExpired;

    public HttpService(String baseUrl, TokenStore tokenStore, Supplier<CompletableFuture<Boolean>> onSessionExpired) {
        this.baseUrl = baseUrl;
        this.tokenStore = tokenStore;
        this.onSessionExpired = onSessionExpired;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    // --- PUBLIC API ---

    public <T> CompletableFuture<T> getAsync(String url, Class<T> responseType) {
        return performRequestAsync(url, "GET", null, responseType);
    }

    public <T, R> CompletableFuture<R> postAsync(String url, T body, Class<R> responseType) {
        return performRequestAsync(url, "POST", body, responseType);
    }

    public <T, R> CompletableFuture<R> putAsync(String url, T body, Class<R> responseType) {
        return performRequestAsync(url, "PUT", body, responseType);
    }

    public <T> CompletableFuture<T> deleteAsync(String url, Class<T> responseType) {
        return performRequestAsync(url, "DELETE", null, responseType);
    }

    public CompletableFuture<Void> subscribeSseAsync(String url, BiConsumer<String, String> eventProcessor) {
        HttpRequest.Builder builder = createRequestBuilder(url, "text/event-stream").GET();
        return executeSseRequest(builder, eventProcessor, false);
    }

    // --- CORE REQUEST LOGIC ---

    private <T, R> CompletableFuture<R> performRequestAsync(
            String url,
            String method,
            T body,
            Class<R> resType
    ) {
        HttpRequest.Builder builder = createRequestBuilder(url, CONTENT_TYPE);

        if (body != null) {
            builder.header("Content-Type", CONTENT_TYPE);
            try {
                builder.method(method, HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)));

            } catch (JacksonException e) {
                return CompletableFuture.failedFuture(new RequestException("Serialization error", e));
            }
        } else {
            builder.method(method, HttpRequest.BodyPublishers.noBody());
        }

        return executeRequest(builder, resType, false);
    }

    private <T> CompletableFuture<T> executeRequest(
            HttpRequest.Builder builder,
            Class<T> resType,
            boolean isRetry
    ) {
        HttpRequest request = builder.build();

        return withExceptionHandling(
                httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                        .thenCompose(res -> processResponse(res, request, builder, resType, isRetry))
        );
    }

    private CompletableFuture<Void> executeSseRequest(
            HttpRequest.Builder builder,
            BiConsumer<String, String> processor,
            boolean isRetry
    ) {
        HttpRequest request = builder.build();
        return withExceptionHandling(
                httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofLines())
                        .thenCompose(res -> processSseResponse(res, request, builder, processor, isRetry))
        );
    }

    // --- RESPONSE PROCESSING ---

    private <T> CompletableFuture<T> processResponse(
            HttpResponse<String> res,
            HttpRequest req,
            HttpRequest.Builder builder,
            Class<T> resType,
            boolean isRetry
    ) {
        logger.info("Response: {} for {}\nBody: {}", res.statusCode(), req.uri(), res.body());

        if (res.statusCode() == 401 && !isRetry && !req.uri().getPath().contains("/auth")) {
            return executeWithRetry(
                    builder,
                    () -> executeRequest(builder, resType, true),
                    res.body()
            );
        }
        if (res.statusCode() >= 300) {
            return handleStandardError(res);
        }
        return handleSuccessfulResponse(res, resType);
    }

    private CompletableFuture<Void> processSseResponse(
            HttpResponse<Stream<String>> res,
            HttpRequest req,
            HttpRequest.Builder builder,
            BiConsumer<String, String> processor,
            boolean isRetry
    ) {
        logger.info("SSE Status: {} for {}", res.statusCode(), req.uri());

        if (res.statusCode() == 401 && !isRetry) {
            return executeWithRetry(builder, () -> executeSseRequest(builder, processor, true), "SSE Server Error (401)");
        }
        if (res.statusCode() >= 300) {
            return CompletableFuture.failedFuture(new RequestException("SSE Server Error (" + res.statusCode() + ")", res.statusCode()));
        }
        return CompletableFuture.runAsync(() -> processSseStream(res.body(), processor));
    }

    // --- AUTH & RETRY LOGIC ---

    private <R> CompletableFuture<R> executeWithRetry(
            HttpRequest.Builder builder,
            Supplier<CompletableFuture<R>> retryAction,
            String errorBody
    ) {
        return handleRefreshToken().thenCompose(success -> {
            if (Boolean.TRUE.equals(success)) {
                applyAuthHeader(builder);
                return retryAction.get();
            }
            // Token refresh fallito: proseguiamo con il login manuale (UI)
            return onSessionExpired.get().thenCompose(manualLoginSuccess -> {
                if (Boolean.TRUE.equals(manualLoginSuccess)) {
                    applyAuthHeader(builder);
                    return retryAction.get();
                }
                return CompletableFuture.failedFuture(new RequestException(errorBody != null ? errorBody : "Unauthorized", 401));
            });
        });
    }

    private CompletableFuture<Boolean> handleRefreshToken() {
        String refreshToken = tokenStore.getRefreshToken();
        if (refreshToken == null) return CompletableFuture.completedFuture(false);

        try {
            TokenDTO dto = new TokenDTO();
            dto.setRefreshToken(refreshToken);

            HttpRequest refreshReq = createRequestBuilder("/auth/refresh", CONTENT_TYPE)
                    .header("Content-Type", CONTENT_TYPE)
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(dto)))
                    .build();

            return httpClient.sendAsync(refreshReq, HttpResponse.BodyHandlers.ofString())
                    .thenApply(res -> {
                        if (res.statusCode() == 200) {
                            try {
                                TokenDTO newToken = objectMapper.readValue(res.body(), TokenDTO.class);
                                tokenStore.setAccessToken(newToken.getAccessToken());
                                if (newToken.getRefreshToken() != null) {
                                    tokenStore.setRefreshToken(newToken.getRefreshToken());
                                }
                                return true;
                            } catch (JacksonException ignored) {
                                // Ignore json parsing error, will logout
                            }
                        }
                        tokenStore.clear();
                        return false;
                    });
        } catch (JacksonException e) {
            return CompletableFuture.completedFuture(false);
        }
    }

    // --- UTILITY METHODS ---

    private HttpRequest.Builder createRequestBuilder(String endpoint, String acceptType) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(baseUrl + endpoint))
                .header("Accept", acceptType);
        applyAuthHeader(builder);
        return builder;
    }

    private void applyAuthHeader(HttpRequest.Builder builder) {
        String token = tokenStore.getAccessToken();
        if (token != null && !token.isBlank()) {
            builder.setHeader("Authorization", "Bearer " + token);
        }
    }

    private <T> CompletableFuture<T> handleStandardError(HttpResponse<String> response) {
        String rawBody = response.body();
        String cleanMessage = "Server error (" + response.statusCode() + ")";

        try {
            if (rawBody != null && !rawBody.isBlank()) {
                ErrorResponseDTO errorDto = objectMapper.readValue(rawBody, ErrorResponseDTO.class);
                cleanMessage = errorDto.getMessage();
            }
        } catch (JacksonException e) {
            logger.warn("Could not parse error response: {}", rawBody);
            cleanMessage = !rawBody.isBlank() ? rawBody : cleanMessage;
        }

        return CompletableFuture.failedFuture(new RequestException(cleanMessage, response.statusCode()));
    }

    private <T> CompletableFuture<T> handleSuccessfulResponse(
            HttpResponse<String> response,
            Class<T> responseType
    ) {
        if (responseType == Void.class || response.body() == null || response.body().isBlank()) {
            return CompletableFuture.completedFuture(null);
        }
        if (responseType == String.class) {
            return CompletableFuture.completedFuture(responseType.cast(response.body()));
        }
        try {
            return CompletableFuture.completedFuture(objectMapper.readValue(response.body(), responseType));
        } catch (JacksonException e) {
            return CompletableFuture.failedFuture(new RequestException("Deserialization error", e));
        }
    }

    private void processSseStream(
            Stream<String> lines,
            BiConsumer<String, String> eventProcessor
    ) {
        StringBuilder dataBuffer = new StringBuilder();
        String currentEventName = "message";

        try (lines) {
            for (String line : (Iterable<String>) lines::iterator) {
                if (line.startsWith("event:")) {
                    currentEventName = line.substring(6).trim();
                } else if (line.startsWith("data:")) {
                    dataBuffer.append(line.substring(5).trim());
                } else if (line.isBlank() && !dataBuffer.isEmpty()) {
                    eventProcessor.accept(currentEventName, dataBuffer.toString());
                    dataBuffer.setLength(0);
                    currentEventName = "message";
                }
            }
        } catch (Exception e) {
            logger.warn("SSE Stream ended or interrupted: {}", e.getMessage());
        }
    }

    private <T> CompletableFuture<T> withExceptionHandling(CompletableFuture<T> future) {
        return future.exceptionally(ex -> {
            Throwable cause = ex;
            while (cause instanceof CompletionException && cause.getCause() != null) {
                cause = cause.getCause();
            }
            if (cause instanceof RequestException) {
                throw new CompletionException(cause);
            }
            throw new CompletionException(new RequestException("Errore di rete. Il server è irraggiungibile", cause));
        });
    }

    public static String extractErrorMessage(Throwable ex) {
        Throwable root = ex;
        while (root instanceof CompletionException && root.getCause() != null) {
            root = root.getCause();
        }
        return root.getMessage() != null ? root.getMessage() : "An unexpected error occurred.";
    }
}