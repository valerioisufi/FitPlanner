package com.example.fitplannerclient.repository;

import com.example.fitplannerclient.controller.session.NotificationObserver;
import com.example.fitplannerclient.service.api.NotificationApi;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotificationRepository {

    private static final Logger logger = LoggerFactory.getLogger(NotificationRepository.class);

    private final NotificationApi notificationApi;

    private final List<String> notifications = new CopyOnWriteArrayList<>();
    private final List<NotificationObserver> observers = new CopyOnWriteArrayList<>();
    
    private CompletableFuture<Void> subscriptionFuture;

    public NotificationRepository(NotificationApi notificationApi) {
        this.notificationApi = notificationApi;
        listenForNotifications(this::processNotification);
    }

    public List<String> getNotifications() {
        return new ArrayList<>(notifications);
    }

    public void attachObserver(NotificationObserver observer){
        observers.add(observer);
    }

    public void detachObserver(NotificationObserver observer){
        observers.remove(observer);
    }

    /**
     * Sottoscrive il client alle notifiche SSE (Server-Sent Events) del server
     */
    private void listenForNotifications(BiConsumer<String, String> eventProcessor) {
        this.subscriptionFuture = notificationApi.subscribe(eventProcessor)
            .exceptionally(ex -> {
                logger.error("Connessione SSE interrotta: {}", ex.getMessage());
                return null;
            });
    }
    
    /**
     * Da chiamare quando l'utente fa il logout per chiudere la connessione pendente.
     */
    public void stopListening() {
        if (subscriptionFuture != null && !subscriptionFuture.isDone()) {
            subscriptionFuture.cancel(true); // Interrompe la richiesta HTTP sottostante
        }
    }

    private void processNotification(String type, String message) {
        notifications.add(message);
        for (NotificationObserver observer : observers) {
            observer.onNotificationReceived();
        }
    }
}
