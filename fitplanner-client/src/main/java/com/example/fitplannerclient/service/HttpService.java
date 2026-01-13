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
    private final String BASE_URL;

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
            BASE_URL = properties.getProperty("api.url");
            if (BASE_URL == null) {
                throw new RuntimeException("La proprietà 'api.url' non è presente in config.properties");
            }
        } catch (IOException ex) {
            throw new RuntimeException("Errore durante la lettura di config.properties", ex);
        }
    }

    public <T> CompletableFuture<T> getAsync(String url, Class<T> responseType){

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + url))
                .header("Accept", "application/json")
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() < 200 || response.statusCode() >= 300) {
                        throw new RuntimeException("Errore HTTP GET: " + response.statusCode());
                    }

                    try {
                        return objectMapper.readValue(response.body(), responseType);
                    } catch (JacksonException e) {
                        throw new RuntimeException("Errore durante la deserializzazione della risposta", e);
                    }
                });
    }

    // Esempio nel Controller
//    HttpService.getInstance().getAsync("users/1", User.class)
//        .thenAccept(user -> {
//            Platform.runLater(() -> {
//                // Aggiorna UI
//            });
//        });

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

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + url))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequestBody))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() < 200 || response.statusCode() >= 300) {
                        throw new RuntimeException("Errore HTTP POST: " + response.statusCode());
                    }

                    try {
                        return objectMapper.readValue(response.body(), responseType);
                    } catch (JacksonException e) {
                        throw new RuntimeException("Errore durante la deserializzazione della risposta", e);
                    }
                });
    }

}
