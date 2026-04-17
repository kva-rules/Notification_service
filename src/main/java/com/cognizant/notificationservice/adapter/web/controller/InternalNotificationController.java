package com.cognizant.notificationservice.adapter.web.controller;

import com.cognizant.notificationservice.application.dto.event.NotificationEvent;
import com.cognizant.notificationservice.application.dto.request.BroadcastNotificationRequest;
import com.cognizant.notificationservice.application.dto.response.NotificationResponse;
import com.cognizant.notificationservice.application.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/notifications")
@RequiredArgsConstructor
@Tag(name = "Internal Notifications", description = "Internal APIs for service-to-service communication")
public class InternalNotificationController {

    private final NotificationService notificationService;

    @PostMapping("/event")
    @Operation(summary = "Process a notification event from other services")
    public ResponseEntity<NotificationResponse> processEvent(@Valid @RequestBody NotificationEvent event) {
        NotificationResponse response = notificationService.processEvent(event);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/broadcast")
    @Operation(summary = "Broadcast a notification to multiple users")
    public ResponseEntity<Void> broadcastNotification(@Valid @RequestBody BroadcastNotificationRequest request) {
        notificationService.broadcastNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
