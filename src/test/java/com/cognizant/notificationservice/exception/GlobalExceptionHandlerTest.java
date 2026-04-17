package com.cognizant.notificationservice.exception;

import com.cognizant.notificationservice.adapter.web.exception.GlobalExceptionHandler;
import com.cognizant.notificationservice.domain.exception.NotificationNotFoundException;
import com.cognizant.notificationservice.domain.exception.NotificationPreferenceNotFoundException;
import com.cognizant.notificationservice.domain.exception.TemplateNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("Should handle NotificationNotFoundException")
    void handleNotificationNotFound() {
        UUID notificationId = UUID.randomUUID();
        NotificationNotFoundException ex = new NotificationNotFoundException(notificationId);

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                exceptionHandler.handleNotificationNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(404);
    }

    @Test
    @DisplayName("Should handle NotificationPreferenceNotFoundException")
    void handlePreferenceNotFound() {
        UUID userId = UUID.randomUUID();
        NotificationPreferenceNotFoundException ex = new NotificationPreferenceNotFoundException(userId);

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                exceptionHandler.handlePreferenceNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(404);
    }

    @Test
    @DisplayName("Should handle TemplateNotFoundException")
    void handleTemplateNotFound() {
        TemplateNotFoundException ex = new TemplateNotFoundException("TICKET_CREATED");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                exceptionHandler.handleTemplateNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(404);
    }

    @Test
    @DisplayName("Should handle validation errors")
    void handleValidationErrors() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("request", "title", "Title is required");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<GlobalExceptionHandler.ValidationErrorResponse> response = 
                exceptionHandler.handleValidationErrors(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().errors()).containsKey("title");
    }

    @Test
    @DisplayName("Should handle AccessDeniedException")
    void handleAccessDenied() {
        AccessDeniedException ex = new AccessDeniedException("Access denied");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                exceptionHandler.handleAccessDenied(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(403);
    }

    @Test
    @DisplayName("Should handle generic exceptions")
    void handleGenericException() {
        Exception ex = new RuntimeException("Unexpected error");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                exceptionHandler.handleGenericException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(500);
    }
}
