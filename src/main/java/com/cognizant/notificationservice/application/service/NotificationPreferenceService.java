package com.cognizant.notificationservice.application.service;

import com.cognizant.notificationservice.application.dto.request.UpdatePreferenceRequest;
import com.cognizant.notificationservice.application.dto.response.NotificationPreferenceResponse;

import java.util.UUID;

public interface NotificationPreferenceService {

    NotificationPreferenceResponse getPreferencesByUserId(UUID userId);

    NotificationPreferenceResponse createDefaultPreferences(UUID userId);

    NotificationPreferenceResponse updatePreferences(UUID userId, UpdatePreferenceRequest request);
}
