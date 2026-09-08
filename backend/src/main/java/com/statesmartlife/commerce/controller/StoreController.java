package com.statesmartlife.commerce.controller;

import com.statesmartlife.common.config.OpenApiConfig;
import com.statesmartlife.common.exception.BusinessRuleException;
import com.statesmartlife.commerce.dto.CommerceCategory;
import com.statesmartlife.commerce.dto.CreateStoreRequest;
import com.statesmartlife.commerce.dto.StoreResponse;
import com.statesmartlife.commerce.service.CommerceService;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Commerce - Stores", description = "Hyperlocal neighborhood store directory APIs")
@RestController
@RequestMapping("/stores")
public class StoreController {

    private final CommerceService commerceService;

    public StoreController(CommerceService commerceService) {
        this.commerceService = commerceService;
    }

    @Operation(summary = "List stores", description = "Retrieves a paginated list of active neighborhood stores, filterable by category.")
    @GetMapping
    public ResponseEntity<Page<StoreResponse>> getStores(
            @RequestParam(required = false) CommerceCategory category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<StoreResponse> response = commerceService.getStores(category, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get store details", description = "Retrieves store information by store ID.")
    @GetMapping("/{id}")
    public ResponseEntity<StoreResponse> getStoreById(@PathVariable("id") UUID id) {
        StoreResponse response = commerceService.getStore(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Register store", description = "Creates a new hyperlocal merchant store for the authenticated shop owner.")
    @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    @PreAuthorize("hasAnyRole('SHOP_OWNER', 'ADMIN', 'MERCHANT')")
    @PostMapping
    public ResponseEntity<StoreResponse> createStore(@Valid @RequestBody CreateStoreRequest request) {
        UUID ownerId = extractAuthenticatedUserId();
        StoreResponse response = commerceService.createStore(ownerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
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
