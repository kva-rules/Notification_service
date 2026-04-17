package com.cognizant.notificationservice.application.mapper;

import com.cognizant.notificationservice.application.dto.request.CreateTemplateRequest;
import com.cognizant.notificationservice.application.dto.response.NotificationTemplateResponse;
import com.cognizant.notificationservice.domain.entity.NotificationTemplate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NotificationTemplateMapper {

    @Mapping(target = "templateId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    NotificationTemplate toEntity(CreateTemplateRequest request);

    NotificationTemplateResponse toResponse(NotificationTemplate template);

    List<NotificationTemplateResponse> toResponseList(List<NotificationTemplate> templates);
}
