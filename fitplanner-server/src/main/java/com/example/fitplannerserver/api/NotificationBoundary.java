package com.example.fitplannerserver.api;

import com.example.fitplannerserver.controller.NotificationController;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/notifications")
public class NotificationBoundary {

    private final NotificationController notificationController;

    public NotificationBoundary(NotificationController notificationController) {
        this.notificationController = notificationController;
    }

    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe() {
        return notificationController.subscribe();
    }
}