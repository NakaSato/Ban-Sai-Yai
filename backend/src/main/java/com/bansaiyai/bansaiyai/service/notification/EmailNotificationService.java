package com.bansaiyai.bansaiyai.service.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationService implements NotificationService {

    private final JavaMailSender emailSender;

    @Value("${app.notification.email.enabled:false}")
    private boolean emailEnabled;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendNotification(String to, String subject, String body) {
        if (!emailEnabled) {
            log.info("Email notification disabled. Mock sending email to '{}' with subject '{}'", to, subject);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            emailSender.send(message);
            log.info("Email sent successfully to '{}'", to);
        } catch (Exception e) {
            log.error("Failed to send email to '{}'", to, e);
            // Don't rethrow to avoid breaking the business transaction
        }
    }
}
