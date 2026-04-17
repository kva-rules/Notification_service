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
public class NotificationPreferenceResponse {

    private UUID preferenceId;
    private UUID userId;
    private boolean emailEnabled;
    private boolean inAppEnabled;
    private boolean ticketUpdates;
    private boolean solutionUpdates;
    private boolean knowledgeUpdates;
    private boolean rewardUpdates;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
