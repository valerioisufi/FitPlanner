package com.example.fitplannerclient.controller.session;

import com.example.fitplannerclient.repository.NotificationRepository;

import java.util.List;

public class NotificationManager {

    private final NotificationRepository notificationRepository;

    public NotificationManager(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public void attachObserver(NotificationObserver observer){
        notificationRepository.attachObserver(observer);
    }

    public void detachObserver(NotificationObserver observer){
        notificationRepository.detachObserver(observer);
    }

    public List<String> getNotifications(){
        return notificationRepository.getNotifications();
    }

    public void stopListening() {
        notificationRepository.stopListening();
    }
}
