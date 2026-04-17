package com.cognizant.notificationservice.domain.exception;

public class TemplateNotFoundException extends RuntimeException {

    public TemplateNotFoundException(String eventType) {
        super("Template not found for event type: " + eventType);
    }
}
