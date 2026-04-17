package com.cognizant.notificationservice.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationTemplateResponse {

    private UUID templateId;
    private String eventType;
    private String titleTemplate;
    private String messageTemplate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
