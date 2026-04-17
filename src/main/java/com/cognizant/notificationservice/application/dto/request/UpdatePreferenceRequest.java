package com.cognizant.notificationservice.application.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePreferenceRequest {

    private Boolean emailEnabled;
    private Boolean inAppEnabled;
    private Boolean ticketUpdates;
    private Boolean solutionUpdates;
    private Boolean knowledgeUpdates;
    private Boolean rewardUpdates;
}
