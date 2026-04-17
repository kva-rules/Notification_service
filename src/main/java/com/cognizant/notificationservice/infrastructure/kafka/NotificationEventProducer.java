package com.cognizant.notificationservice.infrastructure.kafka;

import com.cognizant.notificationservice.application.dto.event.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventProducer {

    private static final String NOTIFICATION_SENT_TOPIC = "notification.sent";
    private static final String NOTIFICATION_FAILED_TOPIC = "notification.failed";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendNotificationEvent(NotificationEvent event) {
        log.info("Sending notification event: {}", event.getEventType());

        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(event.getEventType(), event.getEventType(), event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Notification event sent successfully: {} with offset: {}",
                        event.getEventType(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("Failed to send notification event: {}", event.getEventType(), ex);
            }
        });
    }

    public void publishNotificationSent(UUID notificationId) {
        log.info("Publishing notification.sent event for: {}", notificationId);

        Map<String, Object> payload = Map.of(
                "notificationId", notificationId.toString(),
                "status", "SENT",
                "timestamp", System.currentTimeMillis()
        );

        kafkaTemplate.send(NOTIFICATION_SENT_TOPIC, notificationId.toString(), payload)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("notification.sent event published for: {}", notificationId);
                    } else {
                        log.error("Failed to publish notification.sent event: {}", notificationId, ex);
                    }
                });
    }

    public void publishNotificationFailed(NotificationEvent event, String errorMessage) {
        log.info("Publishing notification.failed event for: {}", event.getEventType());

        Map<String, Object> payload = Map.of(
                "eventType", event.getEventType(),
                "referenceId", event.getReferenceId() != null ? event.getReferenceId().toString() : "",
                "error", errorMessage,
                "timestamp", System.currentTimeMillis()
        );

        kafkaTemplate.send(NOTIFICATION_FAILED_TOPIC, event.getEventType(), payload)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("notification.failed event published for: {}", event.getEventType());
                    } else {
                        log.error("Failed to publish notification.failed event: {}", event.getEventType(), ex);
                    }
                });
    }
}
