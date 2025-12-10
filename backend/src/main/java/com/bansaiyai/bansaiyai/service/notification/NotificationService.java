package com.bansaiyai.bansaiyai.service.notification;

public interface NotificationService {
    void sendNotification(String to, String subject, String body);
}
