package com.example.fitplannerserver.controller;

import com.example.fitplannerserver.security.IdentityProvider;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class NotificationController {

    private final IdentityProvider identityProvider;

    // Map to store emitters per user ID. Thread-safe structures are required.
    private final Map<String, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public NotificationController(IdentityProvider identityProvider) {
        this.identityProvider = identityProvider;
    }


    public SseEmitter subscribe() {
        String userId = this.identityProvider.getUserId();

        // Create a new SseEmitter with a timeout (30 minutes)
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);

        // Add the emitter to the user's list
        this.emitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        // Handle cleanup when the connection is closed, times out, or throws an error
        emitter.onCompletion(() -> removeEmitter(userId, emitter));
        emitter.onTimeout(() -> removeEmitter(userId, emitter));
        emitter.onError((e) -> removeEmitter(userId, emitter));

        try {
            // Send an initial event to immediately establish the connection
            emitter.send(SseEmitter.event().name("INIT").data("Connected successfully"));
        } catch (IOException e) {
            removeEmitter(userId, emitter);
        }

        return emitter;
    }

    private void removeEmitter(String userId, SseEmitter emitter) {
        List<SseEmitter> userEmitters = this.emitters.get(userId);

        if (userEmitters != null) {
            userEmitters.remove(emitter);

            if (userEmitters.isEmpty()) {
                this.emitters.remove(userId);
            }
        }

    }

    public void sendNotificationToUser(String userId, String eventName, String payload) {
        List<SseEmitter> userEmitters = this.emitters.get(userId);

        if (userEmitters != null) {
            List<SseEmitter> deadEmitters = new ArrayList<>();

            for (SseEmitter emitter : userEmitters) {
                try {
                    // Send the custom event to the client
                    emitter.send(SseEmitter.event().name(eventName).data(payload));
                } catch (IOException e) {
                    // If sending fails, the client is probably disconnected
                    deadEmitters.add(emitter);
                }
            }

            // Remove dead connections
            userEmitters.removeAll(deadEmitters);
        }
    }
}
