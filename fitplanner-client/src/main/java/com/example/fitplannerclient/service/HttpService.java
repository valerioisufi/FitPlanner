package com.example.fitplannerclient.service;

import com.example.fitplannerclient.exception.ConfigException;
import com.example.fitplannerclient.exception.RequestException;
import com.example.fitplannercommon.TokenBean;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class HttpService {
    private static final Logger logger = Logger.getLogger(HttpService.class.getName());

    private final SessionManager sessionManager;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    private static final String CONTENT_TYPE = "application/json";
    private final String baseUrl;

    // Callback to trigger when the refresh token is expired/invalid
    private final Runnable onSessionExpired;

    public HttpService(SessionManager sessionManager, Runnable onSessionExpired) {
        this.sessionManager = sessionManager;

        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();

        this.onSessionExpired = onSessionExpired;

        Properties properties = new Properties();

        try (InputStream input = HttpService.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new ConfigException("Impossibile trovare config.properties");
            }
            // Carica le coppie chiave-valore dal file
            properties.load(input);
            baseUrl = properties.getProperty("api.url");
            if (baseUrl == null) {
                throw new ConfigException("La proprietà 'api.url' non è presente in config.properties");
            }
        } catch (IOException ex) {
            throw new ConfigException("Errore durante la lettura di config.properties");
        }
    }

    /**
     * Metodo interno per gestire le richieste
     */
    private <T> CompletableFuture<T> requestAsync(HttpRequest.Builder requestBuilder, Class<T> responseType, boolean isRetry) {
        requestBuilder.header("Accept", CONTENT_TYPE);
        addAuthorizationHeader(requestBuilder);

        HttpRequest request = requestBuilder.build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenCompose(response -> processResponse(response, request, requestBuilder, responseType, isRetry));
    }

    // Handle the authorization header injection
    private void addAuthorizationHeader(HttpRequest.Builder requestBuilder) {
        String token = sessionManager.getAccessToken();
        if (token != null && !token.isEmpty()) {
            requestBuilder.setHeader("Authorization", "Bearer " + token);
        }
    }

    // Route the response based on the HTTP status code
    private <T> CompletableFuture<T> processResponse(HttpResponse<String> response, HttpRequest request, HttpRequest.Builder requestBuilder, Class<T> responseType, boolean isRetry) {
        logger.info("Response code: " + response.statusCode() + " for " + request.uri() + "\nBody: " + response.body());

        if (response.statusCode() == 401 && !isRetry) {
            return handleUnauthorizedRetry(requestBuilder, responseType, response.body());
        }

        if (response.statusCode() >= 300) {
            return handleStandardError(response);
        }

        return handleSuccessfulResponse(response, responseType);
    }

    // Handle token refresh logic and retry mechanism
    private <T> CompletableFuture<T> handleUnauthorizedRetry(HttpRequest.Builder requestBuilder, Class<T> responseType, String responseBody) {
        return handleRefreshToken()
                .thenCompose(success -> {
                    if (success) {
                        requestBuilder.setHeader("Authorization", "Bearer " + sessionManager.getAccessToken());
                        return requestAsync(requestBuilder, responseType, true);
                    } else {
                        // Execute the onSessionExpired callback if the refresh operation fails
                        this.onSessionExpired.run();
                        return CompletableFuture.failedFuture(new RequestException(responseBody));
                    }
                });
    }

    // Handle standard HTTP errors (300+)
    private <T> CompletableFuture<T> handleStandardError(HttpResponse<String> response) {
        String errorMessage = response.body();

        // Fallback to a generic error message if the response body is empty or blank
        if (errorMessage == null || errorMessage.isBlank()) {
            errorMessage = "Server error (" + response.statusCode() + ")";
        }

        return CompletableFuture.failedFuture(new RequestException(errorMessage));
    }

    // Handle successful payload deserialization
    private <T> CompletableFuture<T> handleSuccessfulResponse(HttpResponse<String> response, Class<T> responseType) {
        try {
            T result = objectMapper.readValue(response.body(), responseType);
            return CompletableFuture.completedFuture(result);
        } catch (JacksonException e) {
            return CompletableFuture.failedFuture(new RequestException("Deserialization error", e));
        }
    }

    /**
     * Logica specifica per eseguire il refresh del token.
     * Restituisce true se il refresh ha successo, false altrimenti.
     */
    private CompletableFuture<Boolean> handleRefreshToken() {
        String refreshToken = sessionManager.getRefreshToken();
        if (refreshToken == null) {
            return CompletableFuture.completedFuture(false);
        }

        TokenBean refreshBody = new TokenBean();
        refreshBody.setRefreshToken(refreshToken);

        String jsonBody;
        try {
            jsonBody = objectMapper.writeValueAsString(refreshBody);
        } catch (JacksonException e) {
            return CompletableFuture.completedFuture(false);
        }

        HttpRequest refreshRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/auth/refresh"))
                .header("Content-Type", CONTENT_TYPE)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        return httpClient.sendAsync(refreshRequest, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        try {
                            TokenBean newToken = objectMapper.readValue(response.body(), TokenBean.class);

                            sessionManager.setAccessToken(newToken.getAccessToken());
                            if(newToken.getRefreshToken() != null) {
                                sessionManager.setRefreshToken(newToken.getRefreshToken());
                            }
                            return true;
                        } catch (JacksonException e) {
                            return false;
                        }
                    } else {
                        // Il refresh token è scaduto o non valido -> Logout
                        sessionManager.logout();
                        return false;
                    }
                });
    }

    public <T> CompletableFuture<T> getAsync(String url, Class<T> responseType){
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + url))
                .GET();

        return requestAsync(requestBuilder, responseType, false);
    }

    public <T, R> CompletableFuture<R> postAsync(String url, T requestBody, Class<R> responseType){
        String jsonRequestBody;
        try {
            jsonRequestBody = objectMapper.writeValueAsString(requestBody);
        } catch (JacksonException e) {
            return CompletableFuture.failedFuture(new RequestException("Errore serializzazione", e));
        }

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + url))
                .header("Content-Type", CONTENT_TYPE)
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequestBody));

        return requestAsync(requestBuilder, responseType, false);
    }

    // --- NEW SSE METHODS ---

    /**
     * Subscribes to a Server-Sent Events (SSE) stream.
     *
     * @param url The relative endpoint URL.
     * @param eventProcessor A callback to handle incoming events (eventName, payloadData).
     * @return A CompletableFuture representing the active connection. Call .cancel(true) to close it.
     */
    public CompletableFuture<Void> subscribeSseAsync(String url, BiConsumer<String, String> eventProcessor) {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + url))
                .GET();

        return requestSseAsync(requestBuilder, eventProcessor, false);
    }

    /**
     * Internal method to handle SSE requests, including token refresh logic.
     */
    private CompletableFuture<Void> requestSseAsync(HttpRequest.Builder requestBuilder, BiConsumer<String, String> eventProcessor, boolean isRetry) {
        // SSE requires text/event-stream header
        requestBuilder.header("Accept", "text/event-stream");

        String token = sessionManager.getAccessToken();
        if (token != null && !token.isEmpty()) {
            requestBuilder.setHeader("Authorization", "Bearer " + token);
        }

        HttpRequest request = requestBuilder.build();

        // Use BodyHandlers.ofLines() to read the response as a continuous stream
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofLines())
                .thenCompose(response -> {
                    logger.info("SSE Connection status: " + response.statusCode() + " for " + request.uri());

                    // Handle expired token logic exactly like standard requests
                    if (response.statusCode() == 401 && !isRetry) {
                        return handleRefreshToken()
                                .thenCompose(success -> {
                                    if (success) {
                                        requestBuilder.setHeader("Authorization", "Bearer " + sessionManager.getAccessToken());
                                        return requestSseAsync(requestBuilder, eventProcessor, true);
                                    } else {
                                        this.onSessionExpired.run();
                                        return CompletableFuture.failedFuture(new RequestException("Unauthorized: SSE token refresh failed"));
                                    }
                                });
                    }

                    if (response.statusCode() >= 300) {
                        return CompletableFuture.failedFuture(new RequestException("SSE Server Error (" + response.statusCode() + ")"));
                    }

                    // Process the infinite stream asynchronously on a separate thread
                    return CompletableFuture.runAsync(() -> {
                        String[] currentEventName = {"message"};
                        StringBuilder dataBuffer = new StringBuilder();

                        try (Stream<String> lines = response.body()) {
                            lines.forEach(line -> {
                                if (line.startsWith("event:")) {
                                    currentEventName[0] = line.substring(6).trim();
                                } else if (line.startsWith("data:")) {
                                    // Append data, handling potential multi-line JSON payloads
                                    dataBuffer.append(line.substring(5).trim());
                                } else if (line.isBlank()) {
                                    // An empty line indicates the end of a single SSE block
                                    if (dataBuffer.length() > 0) {
                                        eventProcessor.accept(currentEventName[0], dataBuffer.toString());
                                        dataBuffer.setLength(0); // Reset buffer
                                        currentEventName[0] = "message"; // Reset to default
                                    }
                                }
                            });
                        } catch (Exception e) {
                            logger.warning("SSE Stream ended or interrupted: " + e.getMessage());
                        }
                    });
                });
    }
}