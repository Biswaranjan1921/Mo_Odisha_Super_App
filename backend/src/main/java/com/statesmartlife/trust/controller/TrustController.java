package com.statesmartlife.trust.controller;

import com.statesmartlife.common.exception.BusinessRuleException;
import com.statesmartlife.trust.dto.FileIncidentTicketRequest;
import com.statesmartlife.trust.dto.IncidentTicketResponse;
import com.statesmartlife.trust.dto.ResolveIncidentTicketRequest;
import com.statesmartlife.trust.dto.TrustScoreResponse;
import com.statesmartlife.trust.enums.EntityType;
import com.statesmartlife.trust.enums.TicketStatus;
import com.statesmartlife.trust.service.TrustService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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

@RestController
@RequestMapping("/trust")
@RequiredArgsConstructor
public class TrustController {

    private final TrustService trustService;

    @GetMapping("/score/{entityType}/{entityId}")
    public ResponseEntity<TrustScoreResponse> getTrustScore(
            @PathVariable EntityType entityType,
            @PathVariable UUID entityId) {
        return ResponseEntity.ok(trustService.getTrustScore(entityId, entityType));
    }

    @PostMapping("/incidents")
    public ResponseEntity<IncidentTicketResponse> fileIncidentTicket(
            @Valid @RequestBody FileIncidentTicketRequest request) {
        UUID reporterId = extractAuthenticatedUserId();
        IncidentTicketResponse response = trustService.fileIncidentTicket(reporterId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/incidents/me")
    public ResponseEntity<List<IncidentTicketResponse>> getMyIncidentTickets() {
        UUID reporterId = extractAuthenticatedUserId();
        return ResponseEntity.ok(trustService.getMyIncidentTickets(reporterId));
    }

    @GetMapping("/incidents/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<IncidentTicketResponse>> getAllIncidentTickets() {
        return ResponseEntity.ok(trustService.getAllIncidentTickets());
    }

    @PutMapping("/incidents/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<IncidentTicketResponse> updateTicketStatus(
            @PathVariable UUID id,
            @RequestParam TicketStatus status) {
        UUID adminId = extractAuthenticatedUserId();
        return ResponseEntity.ok(trustService.updateTicketStatus(id, status, adminId));
    }

    @PutMapping("/incidents/{id}/resolve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<IncidentTicketResponse> resolveIncidentTicket(
            @PathVariable UUID id,
            @Valid @RequestBody ResolveIncidentTicketRequest request) {
        UUID adminId = extractAuthenticatedUserId();
        return ResponseEntity.ok(trustService.resolveIncidentTicket(id, request, adminId));
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
}
