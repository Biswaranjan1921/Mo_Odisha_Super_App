package com.statesmartlife.common.exception;

import com.statesmartlife.common.filter.RequestCorrelationFilter;
import com.statesmartlife.common.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Centralized RFC 7807 Global Exception Handler.
 * Converts application exceptions into standardized ErrorResponse envelopes with X-Request-ID tracking.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ErrorResponse> handleBusinessRuleException(BusinessRuleException ex, HttpServletRequest request) {
        return buildResponse(ex.getStatus(), ex.getErrorCode(), ex.getMessage(), request, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        return buildResponse(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Input validation failed", request, errors);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "INVALID_ARGUMENT", ex.getMessage(), request, null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "Access to requested resource is forbidden", request, null);
    }

    @ExceptionHandler(com.statesmartlife.healthcare.exception.DoctorNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleDoctorNotFound(com.statesmartlife.healthcare.exception.DoctorNotFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, "DOCTOR_NOT_FOUND", ex.getMessage(), request, null);
    }

    @ExceptionHandler(com.statesmartlife.healthcare.exception.AppointmentSlotUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleSlotUnavailable(com.statesmartlife.healthcare.exception.AppointmentSlotUnavailableException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, "APPOINTMENT_SLOT_UNAVAILABLE", ex.getMessage(), request, null);
    }

    @ExceptionHandler(com.statesmartlife.healthcare.exception.UnauthorizedHealthcareAccessException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedHealthcareAccess(com.statesmartlife.healthcare.exception.UnauthorizedHealthcareAccessException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.FORBIDDEN, "UNAUTHORIZED_HEALTHCARE_ACCESS", ex.getMessage(), request, null);
    }

    @ExceptionHandler(com.statesmartlife.healthcare.exception.InvalidAppointmentTransitionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidAppointmentTransition(com.statesmartlife.healthcare.exception.InvalidAppointmentTransitionException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "INVALID_APPOINTMENT_TRANSITION", ex.getMessage(), request, null);
    }

    @ExceptionHandler(com.statesmartlife.healthcare.exception.InvalidAppointmentTimeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidAppointmentTime(com.statesmartlife.healthcare.exception.InvalidAppointmentTimeException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "INVALID_APPOINTMENT_TIME", ex.getMessage(), request, null);
    }

    @ExceptionHandler(com.statesmartlife.emergency.exception.EmergencyRequestNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEmergencyNotFound(com.statesmartlife.emergency.exception.EmergencyRequestNotFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, "EMERGENCY_NOT_FOUND", ex.getMessage(), request, null);
    }

    @ExceptionHandler(com.statesmartlife.emergency.exception.InvalidEmergencyTransitionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidEmergencyTransition(com.statesmartlife.emergency.exception.InvalidEmergencyTransitionException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "INVALID_EMERGENCY_TRANSITION", ex.getMessage(), request, null);
    }

    @ExceptionHandler(com.statesmartlife.emergency.exception.UnauthorizedEmergencyAccessException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedEmergencyAccess(com.statesmartlife.emergency.exception.UnauthorizedEmergencyAccessException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.FORBIDDEN, "UNAUTHORIZED_EMERGENCY_ACCESS", ex.getMessage(), request, null);
    }

    @ExceptionHandler(com.statesmartlife.emergency.exception.ActiveEmergencyAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleActiveEmergencyAlreadyExists(com.statesmartlife.emergency.exception.ActiveEmergencyAlreadyExistsException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, "ACTIVE_EMERGENCY_ALREADY_EXISTS", ex.getMessage(), request, null);
    }

    @ExceptionHandler(com.statesmartlife.trust.exception.TrustScoreNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTrustScoreNotFound(com.statesmartlife.trust.exception.TrustScoreNotFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, "TRUST_SCORE_NOT_FOUND", ex.getMessage(), request, null);
    }

    @ExceptionHandler(com.statesmartlife.trust.exception.IncidentTicketNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleIncidentTicketNotFound(com.statesmartlife.trust.exception.IncidentTicketNotFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, "INCIDENT_TICKET_NOT_FOUND", ex.getMessage(), request, null);
    }

    @ExceptionHandler(com.statesmartlife.trust.exception.InvalidDisputeTransitionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidDisputeTransition(com.statesmartlife.trust.exception.InvalidDisputeTransitionException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "INVALID_DISPUTE_TRANSITION", ex.getMessage(), request, null);
    }

    @ExceptionHandler(com.statesmartlife.trust.exception.UnauthorizedTrustAccessException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedTrustAccess(com.statesmartlife.trust.exception.UnauthorizedTrustAccessException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.FORBIDDEN, "UNAUTHORIZED_TRUST_ACCESS", ex.getMessage(), request, null);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Authentication credentials invalid or missing", request, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "An unexpected error occurred", request, null);
    }

    private ResponseEntity<ErrorResponse> buildResponse(
            HttpStatus status,
            String errorCode,
            String message,
            HttpServletRequest request,
            Map<String, String> validationErrors) {

        String requestId = MDC.get(RequestCorrelationFilter.MDC_CORRELATION_KEY);
        if (requestId == null) {
            requestId = request.getHeader(RequestCorrelationFilter.CORRELATION_ID_HEADER);
        }

        ErrorResponse response = ErrorResponse.builder()
                .status(status.value())
                .error(errorCode)
                .message(message)
                .path(request.getRequestURI())
                .requestId(requestId)
                .validationErrors(validationErrors)
                .build();

        return new ResponseEntity<>(response, status);
    }
}
