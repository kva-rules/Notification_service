package com.cognizant.notificationservice.infrastructure.kafka;

import com.cognizant.notificationservice.application.dto.event.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventProducer {

    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;

    @Value("${kafka.topics.notification-events}")
    private String notificationEventsTopic;

    public void sendNotificationEvent(NotificationEvent event) {
        log.info("Sending notification event: {}", event.getEventType());

        CompletableFuture<SendResult<String, NotificationEvent>> future =
                kafkaTemplate.send(notificationEventsTopic, event.getEventType(), event);

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
}
