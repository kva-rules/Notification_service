package com.cognizant.notificationservice.application.mapper;

import com.cognizant.notificationservice.application.dto.response.NotificationPreferenceResponse;
import com.cognizant.notificationservice.domain.entity.NotificationPreference;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NotificationPreferenceMapper {

    NotificationPreferenceResponse toResponse(NotificationPreference preference);
}
