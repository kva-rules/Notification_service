package com.cognizant.notificationservice.application.service;

import java.util.Map;
import java.util.UUID;

public interface EmailService {

    void sendEmail(String to, String subject, String body);

    void sendTemplatedEmail(String to, String templateName, Map<String, String> variables);

    void sendNotificationEmail(UUID userId, String title, String message);
}
