package com.statesmartlife.delivery.controller;

import com.statesmartlife.common.config.OpenApiConfig;
import com.statesmartlife.common.exception.BusinessRuleException;
import com.statesmartlife.delivery.dto.AssignDeliveryPartnerRequest;
import com.statesmartlife.delivery.dto.DeliveryResponse;
import com.statesmartlife.delivery.dto.UpdateDeliveryStatusRequest;
import com.statesmartlife.delivery.service.DeliveryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

import java.util.UUID;
import java.util.stream.Collectors;

@Tag(name = "Logistics - Deliveries", description = "Delivery partner parcel worksheets, partner assignment, and explicit status lifecycle transitions")
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
@RestController
@RequestMapping("/deliveries")
@PreAuthorize("isAuthenticated()")
public class DeliveryController {

    private final DeliveryService deliveryService;

    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @Operation(summary = "Assign delivery partner", description = "Assigns an active delivery partner to a paid order delivery worksheet (Admin or authorized Shop Owner only).")
    @PostMapping("/order/{orderId}/assign")
    public ResponseEntity<DeliveryResponse> assignDeliveryPartner(
            @PathVariable("orderId") UUID orderId,
            @Valid @RequestBody AssignDeliveryPartnerRequest request) {
        UUID requestingUserId = extractAuthenticatedUserId();
        String userRole = extractAuthenticatedUserRole();
        DeliveryResponse response = deliveryService.assignDeliveryPartner(requestingUserId, userRole, orderId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get assigned delivery worksheets", description = "Retrieves paginated delivery worksheets assigned to the authenticated delivery partner.")
    @GetMapping("/assigned")
    public ResponseEntity<Page<DeliveryResponse>> getAssignedDeliveries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        UUID partnerUserId = extractAuthenticatedUserId();
        Pageable pageable = PageRequest.of(page, size);
        Page<DeliveryResponse> response = deliveryService.getAssignedDeliveries(partnerUserId, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Track delivery for order", description = "Retrieves delivery tracking status, historical address snapshots, and timestamps for an order.")
    @GetMapping("/order/{orderId}")
    public ResponseEntity<DeliveryResponse> getDeliveryForOrder(@PathVariable("orderId") UUID orderId) {
        UUID requestingUserId = extractAuthenticatedUserId();
        String userRole = extractAuthenticatedUserRole();
        DeliveryResponse response = deliveryService.getDeliveryForOrder(requestingUserId, userRole, orderId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update delivery status", description = "Transitions delivery status (ASSIGNED -> PICKED_UP -> IN_TRANSIT -> DELIVERED or FAILED) with service-level matrix validation.")
    @PutMapping("/{id}/status")
    public ResponseEntity<DeliveryResponse> updateDeliveryStatus(
            @PathVariable("id") UUID id,
            @Valid @RequestBody UpdateDeliveryStatusRequest request) {
        UUID partnerUserId = extractAuthenticatedUserId();
        String userRole = extractAuthenticatedUserRole();
        DeliveryResponse response = deliveryService.updateDeliveryStatus(partnerUserId, userRole, id, request);
        return ResponseEntity.ok(response);
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
        if (authentication == null || authentication.getAuthorities() == null) {
            return "";
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
    }
}
