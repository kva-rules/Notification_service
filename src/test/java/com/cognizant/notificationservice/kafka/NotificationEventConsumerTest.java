package com.cognizant.notificationservice.kafka;

import com.cognizant.notificationservice.application.dto.event.NotificationEvent;
import com.cognizant.notificationservice.application.dto.response.NotificationResponse;
import com.cognizant.notificationservice.application.dto.response.NotificationTemplateResponse;
import com.cognizant.notificationservice.application.service.NotificationService;
import com.cognizant.notificationservice.application.service.NotificationTemplateService;
import com.cognizant.notificationservice.domain.enums.NotificationStatus;
import com.cognizant.notificationservice.domain.enums.NotificationType;
import com.cognizant.notificationservice.infrastructure.kafka.NotificationEventConsumer;
import com.cognizant.notificationservice.infrastructure.kafka.NotificationEventProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationEventConsumerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationTemplateService templateService;

    @Mock
    private NotificationEventProducer eventProducer;

    @InjectMocks
    private NotificationEventConsumer eventConsumer;

    private NotificationEvent event;
    private NotificationResponse notificationResponse;
    private NotificationTemplateResponse templateResponse;

    @BeforeEach
    void setUp() {
        UUID notificationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        event = NotificationEvent.builder()
                .eventType("TICKET_CREATED")
                .referenceId(UUID.randomUUID())
                .referenceType("TICKET")
                .recipientUserIds(List.of(userId))
                .templateVariables(Map.of("ticketId", "TKT-001", "message", "New ticket created"))
                .build();

        notificationResponse = NotificationResponse.builder()
                .notificationId(notificationId)
                .title("Ticket Created")
                .message("New ticket TKT-001 created")
                .type(NotificationType.TICKET_CREATED)
                .status(NotificationStatus.CREATED)
                .build();

        templateResponse = NotificationTemplateResponse.builder()
                .templateId(UUID.randomUUID())
                .eventType("TICKET_CREATED")
                .titleTemplate("Ticket Created: {{ticketId}}")
                .messageTemplate("{{message}}")
                .build();
    }

    @Test
    @DisplayName("Should process ticket created event successfully")
    void consumeTicketCreated_Success() {
        when(templateService.getTemplateByEventType("TICKET_CREATED")).thenReturn(templateResponse);
        when(notificationService.createNotification(any())).thenReturn(notificationResponse);
        doNothing().when(notificationService).sendNotification(any());
        doNothing().when(eventProducer).publishNotificationSent(any());

        eventConsumer.consumeTicketCreated(event);

        verify(notificationService).createNotification(any());
        verify(notificationService).sendNotification(notificationResponse.getNotificationId());
        verify(eventProducer).publishNotificationSent(notificationResponse.getNotificationId());
    }

    @Test
    @DisplayName("Should process ticket assigned event")
    void consumeTicketAssigned_Success() {
        event = NotificationEvent.builder()
                .eventType("TICKET_ASSIGNED")
                .referenceId(UUID.randomUUID())
                .referenceType("TICKET")
                .recipientUserIds(List.of(UUID.randomUUID()))
                .templateVariables(Map.of("ticketId", "TKT-002"))
                .build();

        when(templateService.getTemplateByEventType("TICKET_ASSIGNED")).thenReturn(templateResponse);
        when(notificationService.createNotification(any())).thenReturn(notificationResponse);
        doNothing().when(notificationService).sendNotification(any());
        doNothing().when(eventProducer).publishNotificationSent(any());

        eventConsumer.consumeTicketAssigned(event);

        verify(notificationService).createNotification(any());
    }

    @Test
    @DisplayName("Should process reward points added event")
    void consumeRewardPointsAdded_Success() {
        event = NotificationEvent.builder()
                .eventType("REWARD_POINTS_ADDED")
                .referenceId(UUID.randomUUID())
                .referenceType("REWARD")
                .recipientUserIds(List.of(UUID.randomUUID()))
                .templateVariables(Map.of("points", "100"))
                .build();

        when(templateService.getTemplateByEventType("REWARD_POINTS_ADDED")).thenReturn(templateResponse);
        when(notificationService.createNotification(any())).thenReturn(notificationResponse);
        doNothing().when(notificationService).sendNotification(any());
        doNothing().when(eventProducer).publishNotificationSent(any());

        eventConsumer.consumeRewardPointsAdded(event);

        verify(notificationService).createNotification(any());
    }

    @Test
    @DisplayName("Should publish failed event on error")
    void consumeTicketCreated_Error_PublishFailed() {
        when(templateService.getTemplateByEventType("TICKET_CREATED")).thenReturn(templateResponse);
        when(notificationService.createNotification(any())).thenThrow(new RuntimeException("Database error"));
        doNothing().when(eventProducer).publishNotificationFailed(any(), anyString());

        eventConsumer.consumeTicketCreated(event);

        verify(eventProducer).publishNotificationFailed(eq(event), anyString());
    }
}
