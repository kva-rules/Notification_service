package com.cognizant.notificationservice.domain.exception;

import java.util.UUID;

public class NotificationPreferenceNotFoundException extends RuntimeException {

    public NotificationPreferenceNotFoundException(UUID userId) {
        super("Notification preference not found for user: " + userId);
    }
}
