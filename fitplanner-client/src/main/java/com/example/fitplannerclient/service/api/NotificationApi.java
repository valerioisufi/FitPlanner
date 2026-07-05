package com.example.fitplannerclient.service.api;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public interface NotificationApi {
    CompletableFuture<Void> subscribe(BiConsumer<String, String> eventProcessor);
}
