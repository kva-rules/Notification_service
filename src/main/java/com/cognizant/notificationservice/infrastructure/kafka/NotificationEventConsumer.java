package com.cognizant.notificationservice.infrastructure.kafka;

import com.cognizant.notificationservice.application.dto.event.NotificationEvent;
import com.cognizant.notificationservice.application.dto.request.CreateNotificationRequest;
import com.cognizant.notificationservice.application.dto.response.NotificationTemplateResponse;
import com.cognizant.notificationservice.application.service.NotificationService;
import com.cognizant.notificationservice.application.service.NotificationTemplateService;
import com.cognizant.notificationservice.domain.enums.NotificationType;
import com.cognizant.notificationservice.domain.exception.TemplateNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventConsumer {

    private final NotificationService notificationService;
    private final NotificationTemplateService templateService;

    @KafkaListener(topics = "${kafka.topics.notification-events}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeNotificationEvent(NotificationEvent event) {
        log.info("Received notification event: {}", event.getEventType());

        try {
            NotificationTemplateResponse template = templateService.getTemplateByEventType(event.getEventType());

            String title = processTemplate(template.getTitleTemplate(), event.getTemplateVariables());
            String message = processTemplate(template.getMessageTemplate(), event.getTemplateVariables());

            CreateNotificationRequest request = CreateNotificationRequest.builder()
                    .title(title)
                    .message(message)
                    .type(NotificationType.valueOf(event.getEventType()))
                    .referenceId(event.getReferenceId())
                    .referenceType(event.getReferenceType())
                    .recipientUserIds(event.getRecipientUserIds())
                    .build();

            notificationService.createNotification(request);
            log.info("Successfully processed notification event: {}", event.getEventType());

        } catch (TemplateNotFoundException e) {
            log.error("Template not found for event type: {}", event.getEventType(), e);
        } catch (Exception e) {
            log.error("Error processing notification event: {}", event.getEventType(), e);
        }
    }

    private String processTemplate(String template, Map<String, String> variables) {
        if (variables == null || variables.isEmpty()) {
            return template;
        }

        String result = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return result;
    }
}
