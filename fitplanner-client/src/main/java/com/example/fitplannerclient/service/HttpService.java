package com.example.fitplannerclient.service;

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

public class HttpService {

    private static class Wrapper {
        public static final HttpService INSTANCE = new HttpService();
    }

    public static HttpService getInstance(){
        return Wrapper.INSTANCE;
    }

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    private final String contentType = "application/json";
    private final String baseUrl;

    private HttpService() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();

        Properties properties = new Properties();

        try (InputStream input = HttpService.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new RuntimeException("Impossibile trovare config.properties");
            }
            // Carica le coppie chiave-valore dal file
            properties.load(input);
            baseUrl = properties.getProperty("api.url");
            if (baseUrl == null) {
                throw new RuntimeException("La proprietà 'api.url' non è presente in config.properties");
            }
        } catch (IOException ex) {
            throw new RuntimeException("Errore durante la lettura di config.properties", ex);
        }
    }


    private <T> CompletableFuture<T> requestAsync(HttpRequest.Builder requestBuilder, Class<T> responseType){
        requestBuilder.header("Accept", contentType);

        if (SessionManager.getInstance().getAccessToken() != null) {
            requestBuilder.header("Authorization", "Bearer " + SessionManager.getInstance().getAccessToken());
        }

        HttpRequest request = requestBuilder.build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() < 200 || response.statusCode() >= 300) {
                        throw new RuntimeException("Errore HTTP: " + response.statusCode());
                    }

                    try {
                        return objectMapper.readValue(response.body(), responseType);
                    } catch (JacksonException e) {
                        throw new RuntimeException("Errore durante la deserializzazione della risposta", e);
                    }
                });
    }

    public <T> CompletableFuture<T> getAsync(String url, Class<T> responseType){
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + url))
                .GET();

        return requestAsync(requestBuilder, responseType);
    }

    public <T, R> CompletableFuture<R> postAsync(String url, T requestBody, Class<R> responseType){
        String jsonRequestBody;
        try {
            jsonRequestBody = objectMapper.writeValueAsString(requestBody);
        } catch (JacksonException e) {
            // Se non riusciamo a convertire l'oggetto in JSON, falliamo subito
            CompletableFuture<R> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(new RuntimeException("Errore serializzazione body", e));
            return failedFuture;
        }

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + url))
                .header("Content-Type", contentType)
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequestBody));

        return requestAsync(requestBuilder, responseType);
    }

}
