package com.cognizant.notificationservice.adapter.web.controller;

import com.cognizant.notificationservice.application.dto.request.UpdatePreferenceRequest;
import com.cognizant.notificationservice.application.dto.response.NotificationPreferenceResponse;
import com.cognizant.notificationservice.application.service.NotificationPreferenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications/preferences")
@RequiredArgsConstructor
@Tag(name = "Notification Preferences", description = "Notification preference management APIs")
public class NotificationPreferenceController {

    private final NotificationPreferenceService preferenceService;

    @GetMapping
    @Operation(summary = "Get notification preferences for the authenticated user")
    public ResponseEntity<NotificationPreferenceResponse> getPreferences(
            @AuthenticationPrincipal String userId) {
        NotificationPreferenceResponse response =
                preferenceService.getPreferencesByUserId(UUID.fromString(userId));
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @Operation(summary = "Create default notification preferences for the authenticated user")
    public ResponseEntity<NotificationPreferenceResponse> createDefaultPreferences(
            @AuthenticationPrincipal String userId) {
        NotificationPreferenceResponse response =
                preferenceService.createDefaultPreferences(UUID.fromString(userId));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping
    @Operation(summary = "Update notification preferences for the authenticated user")
    public ResponseEntity<NotificationPreferenceResponse> updatePreferences(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody UpdatePreferenceRequest request) {
        NotificationPreferenceResponse response =
                preferenceService.updatePreferences(UUID.fromString(userId), request);
        return ResponseEntity.ok(response);
    }
}
