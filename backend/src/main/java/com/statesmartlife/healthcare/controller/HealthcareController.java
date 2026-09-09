package com.statesmartlife.healthcare.controller;

import com.statesmartlife.common.exception.BusinessRuleException;
import com.statesmartlife.healthcare.dto.AppointmentResponse;
import com.statesmartlife.healthcare.dto.BookAppointmentRequest;
import com.statesmartlife.healthcare.dto.DoctorResponse;
import com.statesmartlife.healthcare.dto.DoctorScheduleResponse;
import com.statesmartlife.healthcare.dto.HospitalResponse;
import com.statesmartlife.healthcare.dto.UpdateAppointmentStatusRequest;
import com.statesmartlife.healthcare.dto.UpdatePrescriptionRequest;
import com.statesmartlife.healthcare.service.HealthcareService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/healthcare")
public class HealthcareController {

    private final HealthcareService healthcareService;

    public HealthcareController(HealthcareService healthcareService) {
        this.healthcareService = healthcareService;
    }

    @GetMapping("/hospitals")
    public ResponseEntity<List<HospitalResponse>> getAllHospitals() {
        return ResponseEntity.ok(healthcareService.getAllHospitals());
    }

    @GetMapping("/doctors")
    public ResponseEntity<Page<DoctorResponse>> getDoctors(
            @RequestParam(required = false) String specialization,
            @RequestParam(required = false) UUID hospitalId,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(healthcareService.getDoctors(specialization, hospitalId, pageable));
    }

    @GetMapping("/doctors/{doctorId}")
    public ResponseEntity<DoctorResponse> getDoctorById(@PathVariable UUID doctorId) {
        return ResponseEntity.ok(healthcareService.getDoctorById(doctorId));
    }

    @GetMapping("/doctors/{doctorId}/schedules")
    public ResponseEntity<List<DoctorScheduleResponse>> getDoctorSchedules(@PathVariable UUID doctorId) {
        return ResponseEntity.ok(healthcareService.getDoctorSchedules(doctorId));
    }

    @PostMapping("/appointments")
    public ResponseEntity<AppointmentResponse> bookAppointment(@Valid @RequestBody BookAppointmentRequest request) {
        UUID currentUserId = extractAuthenticatedUserId();
        AppointmentResponse response = healthcareService.bookAppointment(currentUserId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/appointments/me")
    public ResponseEntity<Page<AppointmentResponse>> getMyAppointments(@PageableDefault(size = 10) Pageable pageable) {
        UUID currentUserId = extractAuthenticatedUserId();
        return ResponseEntity.ok(healthcareService.getCitizenAppointments(currentUserId, pageable));
    }

    @GetMapping("/appointments/doctor")
    @PreAuthorize("hasAnyRole('HEALTHCARE_PROVIDER', 'ADMIN')")
    public ResponseEntity<Page<AppointmentResponse>> getDoctorAppointments(@PageableDefault(size = 10) Pageable pageable) {
        UUID currentUserId = extractAuthenticatedUserId();
        String role = extractAuthenticatedUserRole();
        return ResponseEntity.ok(healthcareService.getDoctorAppointments(currentUserId, role, pageable));
    }

    @PutMapping("/appointments/{id}/status")
    public ResponseEntity<AppointmentResponse> updateAppointmentStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAppointmentStatusRequest request) {
        UUID currentUserId = extractAuthenticatedUserId();
        String role = extractAuthenticatedUserRole();
        return ResponseEntity.ok(healthcareService.updateAppointmentStatus(currentUserId, role, id, request));
    }

    @PutMapping("/appointments/{id}/prescription")
    @PreAuthorize("hasAnyRole('HEALTHCARE_PROVIDER', 'ADMIN')")
    public ResponseEntity<AppointmentResponse> issuePrescription(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePrescriptionRequest request) {
        UUID currentUserId = extractAuthenticatedUserId();
        String role = extractAuthenticatedUserRole();
        return ResponseEntity.ok(healthcareService.issuePrescription(currentUserId, role, id, request));
    }

    private UUID extractAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null || authentication.getName().equals("anonymousUser")) {
            throw new BusinessRuleException("UNAUTHORIZED", "User is not authenticated", HttpStatus.UNAUTHORIZED);
        }
        try {
            return UUID.fromString(authentication.getName());
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleException("INVALID_USER_ID", "Invalid user security principal", HttpStatus.UNAUTHORIZED);
        }
    }

    private String extractAuthenticatedUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return "";
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
    }
}
