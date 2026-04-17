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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/preferences")
@RequiredArgsConstructor
@Tag(name = "Notification Preferences", description = "Notification preference management APIs")
public class NotificationPreferenceController {

    private final NotificationPreferenceService preferenceService;

    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyRole('ENGINEER', 'ADMIN')")
    @Operation(summary = "Get notification preferences for a user")
    public ResponseEntity<NotificationPreferenceResponse> getPreferences(@PathVariable UUID userId) {
        NotificationPreferenceResponse response = preferenceService.getPreferencesByUserId(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasAnyRole('ENGINEER', 'ADMIN')")
    @Operation(summary = "Update notification preferences for a user")
    public ResponseEntity<NotificationPreferenceResponse> updatePreferences(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdatePreferenceRequest request) {
        NotificationPreferenceResponse response = preferenceService.updatePreferences(userId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{userId}")
    @PreAuthorize("hasAnyRole('ENGINEER', 'ADMIN')")
    @Operation(summary = "Create default notification preferences for a user")
    public ResponseEntity<NotificationPreferenceResponse> createDefaultPreferences(@PathVariable UUID userId) {
        NotificationPreferenceResponse response = preferenceService.createDefaultPreferences(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
