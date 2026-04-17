package com.cognizant.notificationservice.application.service;

import com.cognizant.notificationservice.application.dto.request.CreateTemplateRequest;
import com.cognizant.notificationservice.application.dto.response.NotificationTemplateResponse;

import java.util.List;
import java.util.UUID;

public interface NotificationTemplateService {

    NotificationTemplateResponse createTemplate(CreateTemplateRequest request);

    NotificationTemplateResponse getTemplateById(UUID templateId);

    NotificationTemplateResponse getTemplateByEventType(String eventType);

    List<NotificationTemplateResponse> getAllTemplates();

    NotificationTemplateResponse updateTemplate(UUID templateId, CreateTemplateRequest request);

    void deleteTemplate(UUID templateId);
}
