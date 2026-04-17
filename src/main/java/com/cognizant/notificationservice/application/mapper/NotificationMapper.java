package com.cognizant.notificationservice.application.mapper;

import com.cognizant.notificationservice.application.dto.request.CreateNotificationRequest;
import com.cognizant.notificationservice.application.dto.response.NotificationResponse;
import com.cognizant.notificationservice.application.dto.response.UserNotificationResponse;
import com.cognizant.notificationservice.domain.entity.Notification;
import com.cognizant.notificationservice.domain.entity.NotificationRecipient;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NotificationMapper {

    @Mapping(target = "notificationId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "recipients", ignore = true)
    @Mapping(target = "activityLogs", ignore = true)
    @Mapping(target = "deliveryLogs", ignore = true)
    Notification toEntity(CreateNotificationRequest request);

    NotificationResponse toResponse(Notification notification);

    List<NotificationResponse> toResponseList(List<Notification> notifications);

    @Mapping(target = "notificationId", source = "notification.notificationId")
    @Mapping(target = "title", source = "notification.title")
    @Mapping(target = "message", source = "notification.message")
    @Mapping(target = "type", source = "notification.type")
    @Mapping(target = "referenceId", source = "notification.referenceId")
    @Mapping(target = "referenceType", source = "notification.referenceType")
    @Mapping(target = "createdAt", source = "notification.createdAt")
    @Mapping(target = "read", source = "recipient.read")
    @Mapping(target = "readAt", source = "recipient.readAt")
    UserNotificationResponse toUserNotificationResponse(Notification notification, NotificationRecipient recipient);
}
