package com.cognizant.notificationservice.adapter.web.controller;

import com.cognizant.notificationservice.application.dto.event.NotificationEvent;
import com.cognizant.notificationservice.application.dto.request.BroadcastNotificationRequest;
import com.cognizant.notificationservice.application.dto.response.NotificationResponse;
import com.cognizant.notificationservice.application.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications (Internal)", description = "Service-to-service notification triggering")
public class InternalNotificationController {

    private final NotificationService notificationService;

    @PostMapping("/event")
    @Operation(summary = "Process a notification event", description = "Ingest an event pushed by another service")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Event processed and notification created"),
            @ApiResponse(responseCode = "400", description = "Invalid event payload")
    })
    public ResponseEntity<NotificationResponse> processEvent(@Valid @RequestBody NotificationEvent event) {
        NotificationResponse response = notificationService.processEvent(event);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/broadcast")
    @Operation(summary = "Broadcast notification to users", description = "Fan-out one notification to many users")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Broadcast accepted"),
            @ApiResponse(responseCode = "400", description = "Invalid broadcast request")
    })
    public ResponseEntity<Void> broadcastNotification(@Valid @RequestBody BroadcastNotificationRequest request) {
        notificationService.broadcastNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
