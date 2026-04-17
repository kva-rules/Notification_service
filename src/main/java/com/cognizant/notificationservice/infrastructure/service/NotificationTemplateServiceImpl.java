package com.cognizant.notificationservice.infrastructure.service;

import com.cognizant.notificationservice.application.dto.request.CreateTemplateRequest;
import com.cognizant.notificationservice.application.dto.response.NotificationTemplateResponse;
import com.cognizant.notificationservice.application.mapper.NotificationTemplateMapper;
import com.cognizant.notificationservice.application.service.NotificationTemplateService;
import com.cognizant.notificationservice.domain.entity.NotificationTemplate;
import com.cognizant.notificationservice.domain.exception.TemplateNotFoundException;
import com.cognizant.notificationservice.infrastructure.repository.NotificationTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationTemplateServiceImpl implements NotificationTemplateService {

    private final NotificationTemplateRepository templateRepository;
    private final NotificationTemplateMapper templateMapper;

    @Override
    public NotificationTemplateResponse createTemplate(CreateTemplateRequest request) {
        NotificationTemplate template = templateMapper.toEntity(request);
        NotificationTemplate savedTemplate = templateRepository.save(template);
        log.info("Created template for event type: {}", request.getEventType());
        return templateMapper.toResponse(savedTemplate);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationTemplateResponse getTemplateById(UUID templateId) {
        NotificationTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new TemplateNotFoundException("Template not found with id: " + templateId));
        return templateMapper.toResponse(template);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationTemplateResponse getTemplateByEventType(String eventType) {
        NotificationTemplate template = templateRepository.findByEventType(eventType)
                .orElseThrow(() -> new TemplateNotFoundException(eventType));
        return templateMapper.toResponse(template);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationTemplateResponse> getAllTemplates() {
        return templateMapper.toResponseList(templateRepository.findAll());
    }

    @Override
    public NotificationTemplateResponse updateTemplate(UUID templateId, CreateTemplateRequest request) {
        NotificationTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new TemplateNotFoundException("Template not found with id: " + templateId));

        template.setEventType(request.getEventType());
        template.setTitleTemplate(request.getTitleTemplate());
        template.setMessageTemplate(request.getMessageTemplate());

        NotificationTemplate updatedTemplate = templateRepository.save(template);
        log.info("Updated template: {}", templateId);

        return templateMapper.toResponse(updatedTemplate);
    }

    @Override
    public void deleteTemplate(UUID templateId) {
        if (!templateRepository.existsById(templateId)) {
            throw new TemplateNotFoundException("Template not found with id: " + templateId);
        }
        templateRepository.deleteById(templateId);
        log.info("Deleted template: {}", templateId);
    }
}
