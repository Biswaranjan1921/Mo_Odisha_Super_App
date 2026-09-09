package com.statesmartlife.emergency.controller;

import com.statesmartlife.common.exception.BusinessRuleException;
import com.statesmartlife.emergency.dto.DispatchVehicleRequest;
import com.statesmartlife.emergency.dto.EmergencyResponse;
import com.statesmartlife.emergency.dto.TriggerSosRequest;
import com.statesmartlife.emergency.dto.UpdateEmergencyStatusRequest;
import com.statesmartlife.emergency.service.EmergencyService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/emergency")
public class EmergencyController {

    private final EmergencyService emergencyService;

    public EmergencyController(EmergencyService emergencyService) {
        this.emergencyService = emergencyService;
    }

    @PostMapping("/sos")
    public ResponseEntity<EmergencyResponse> triggerSos(@Valid @RequestBody TriggerSosRequest request) {
        UUID requesterId = extractAuthenticatedUserId();
        EmergencyResponse response = emergencyService.triggerSos(requesterId, null, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<Page<EmergencyResponse>> getMyEmergencyRequests(@PageableDefault(size = 10) Pageable pageable) {
        UUID requesterId = extractAuthenticatedUserId();
        return ResponseEntity.ok(emergencyService.getMyEmergencyRequests(requesterId, pageable));
    }

    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<EmergencyResponse>> getActiveEmergencyRequests(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(emergencyService.getActiveEmergencyRequests(pageable));
    }

    @PutMapping("/{id}/dispatch")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmergencyResponse> dispatchVehicle(
            @PathVariable UUID id,
            @Valid @RequestBody DispatchVehicleRequest request) {
        UUID currentUserId = extractAuthenticatedUserId();
        String role = extractAuthenticatedUserRole();
        return ResponseEntity.ok(emergencyService.dispatchVehicle(currentUserId, role, id, request));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<EmergencyResponse> updateEmergencyStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateEmergencyStatusRequest request) {
        UUID currentUserId = extractAuthenticatedUserId();
        String role = extractAuthenticatedUserRole();
        return ResponseEntity.ok(emergencyService.updateEmergencyStatus(currentUserId, role, id, request));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<EmergencyResponse> cancelEmergencyRequest(@PathVariable UUID id) {
        UUID currentUserId = extractAuthenticatedUserId();
        String role = extractAuthenticatedUserRole();
        return ResponseEntity.ok(emergencyService.cancelEmergencyRequest(currentUserId, role, id));
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
