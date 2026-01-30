package com.example.fitplannerclient.service;

import com.example.fitplannerclient.Navigator;
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
import java.util.logging.Logger;

public class HttpService {
    private static final Logger logger = Logger.getLogger(HttpService.class.getName());

    private static class Wrapper {
        public static final HttpService INSTANCE = new HttpService();
    }

    public static HttpService getInstance(){
        return Wrapper.INSTANCE;
    }

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    private static final String CONTENT_TYPE = "application/json";
    private final String baseUrl;

    private HttpService() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();

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
        String token = SessionManager.getInstance().getAccessToken();
        if (token != null && !token.isEmpty()) {
            requestBuilder.setHeader("Authorization", "Bearer " + token);
        }

        HttpRequest request = requestBuilder.build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenCompose(response -> {
                    logger.info("Response code: " + response.statusCode() + " for " + request.uri() + "\nBody: " + response.body());

                    // Token Scaduto (401) e non stiamo già riprovando
                    if (response.statusCode() == 401 && !isRetry) {
                        return handleRefreshToken()
                                .thenCompose(success -> {
                                    if (success) {
                                        requestBuilder.setHeader("Authorization", "Bearer " + SessionManager.getInstance().getAccessToken());
                                        return requestAsync(requestBuilder, responseType, true);
                                    } else {
                                        // Se il refresh fallisce, propaga l'errore (Logout forzato)
                                        Navigator.getInstance().startHomeController();
                                        String errorMessage = response.body();
                                        return CompletableFuture.failedFuture(new RequestException(errorMessage));
                                    }
                                });
                    }

                    // Gestione Errori Standard
                    if (response.statusCode() >= 300) {
                        String errorMessage = response.body();

                        // Se il body è vuoto, usa un fallback generico
                        if (errorMessage == null || errorMessage.isBlank()) {
                            errorMessage = "Errore del server (" + response.statusCode() + ")";
                        }

                        return CompletableFuture.failedFuture(new RequestException(errorMessage));
                    }

                    // Successo
                    try {
                        T result = objectMapper.readValue(response.body(), responseType);
                        return CompletableFuture.completedFuture(result);
                    } catch (JacksonException e) {
                        return CompletableFuture.failedFuture(new RequestException("Errore deserializzazione", e));
                    }
                });
    }

    /**
     * Logica specifica per eseguire il refresh del token.
     * Restituisce true se il refresh ha successo, false altrimenti.
     */
    private CompletableFuture<Boolean> handleRefreshToken() {
        String refreshToken = SessionManager.getInstance().getRefreshToken();
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

                            SessionManager.getInstance().setAccessToken(newToken.getAccessToken());
                            if(newToken.getRefreshToken() != null) {
                                SessionManager.getInstance().setRefreshToken(newToken.getRefreshToken());
                            }
                            return true;
                        } catch (JacksonException e) {
                            return false;
                        }
                    } else {
                        // Il refresh token è scaduto o non valido -> Logout
                        SessionManager.getInstance().logout();
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

}
