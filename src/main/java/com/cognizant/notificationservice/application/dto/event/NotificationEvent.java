package com.cognizant.notificationservice.application.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {

    private String eventType;
    private UUID referenceId;
    private String referenceType;
    private List<UUID> recipientUserIds;
    private Map<String, String> templateVariables;
}
