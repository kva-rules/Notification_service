package com.cognizant.notificationservice.application.dto.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RewardPointsAddedEvent {
    private UUID userId;
    private Integer pointsAdded;
    private Integer totalPoints;
    private String reason;
    private UUID referenceId;
    private Long timestamp;
}
