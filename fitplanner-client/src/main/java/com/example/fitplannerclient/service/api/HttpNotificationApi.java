package com.example.fitplannerclient.service.api;

import com.example.fitplannerclient.service.HttpService;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class HttpNotificationApi implements NotificationApi {

    private final HttpService httpService;
    private static final String NOTIFICATION_ENDPOINT = "/notifications/subscribe";

    public HttpNotificationApi(HttpService httpService) {
        this.httpService = httpService;
    }

    @Override
    public CompletableFuture<Void> subscribe(BiConsumer<String, String> eventProcessor) {
        // Usa il metodo già predisposto nell'HttpService per le SSE (Server-Sent Events)
        return httpService.subscribeSseAsync(NOTIFICATION_ENDPOINT, eventProcessor);
    }
}
