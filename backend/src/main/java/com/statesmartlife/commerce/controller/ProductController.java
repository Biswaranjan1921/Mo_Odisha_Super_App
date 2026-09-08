package com.statesmartlife.commerce.controller;

import com.statesmartlife.common.config.OpenApiConfig;
import com.statesmartlife.common.exception.BusinessRuleException;
import com.statesmartlife.commerce.dto.CreateProductRequest;
import com.statesmartlife.commerce.dto.DeductStockRequest;
import com.statesmartlife.commerce.dto.ProductResponse;
import com.statesmartlife.commerce.dto.UpdateInventoryRequest;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Commerce - Products & Inventory", description = "Hyperlocal product cataloging and inventory stock APIs")
@RestController
@RequestMapping("/products")
public class ProductController {

    private final CommerceService commerceService;

    public ProductController(CommerceService commerceService) {
        this.commerceService = commerceService;
    }

    @Operation(summary = "List all products", description = "Retrieves a paginated list of products across active stores.")
    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductResponse> response = commerceService.getProducts(pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "List products by store", description = "Retrieves a paginated list of products belonging to a specific store.")
    @GetMapping("/store/{storeId}")
    public ResponseEntity<Page<ProductResponse>> getProductsByStore(
            @PathVariable("storeId") UUID storeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductResponse> response = commerceService.getProductsByStore(storeId, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Add product to store catalog", description = "Adds a new product to a store catalog. Asserts shop owner ownership.")
    @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    @PreAuthorize("hasAnyRole('SHOP_OWNER', 'ADMIN', 'MERCHANT')")
    @PostMapping("/store/{storeId}")
    public ResponseEntity<ProductResponse> createProduct(
            @PathVariable("storeId") UUID storeId,
            @Valid @RequestBody CreateProductRequest request) {
        UUID ownerId = extractAuthenticatedUserId();
        ProductResponse response = commerceService.createProduct(ownerId, storeId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Update product inventory stock", description = "Sets absolute stock quantity for a product. Asserts shop owner ownership.")
    @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    @PreAuthorize("hasAnyRole('SHOP_OWNER', 'ADMIN', 'MERCHANT')")
    @PutMapping("/{id}/inventory")
    public ResponseEntity<Void> updateInventory(
            @PathVariable("id") UUID productId,
            @Valid @RequestBody UpdateInventoryRequest request) {
        UUID ownerId = extractAuthenticatedUserId();
        commerceService.updateInventory(ownerId, productId, request.getStockQuantity());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Deduct inventory stock atomically", description = "Atomically reduces inventory stock with concurrency safety.")
    @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    @PutMapping("/{id}/inventory/deduct")
    public ResponseEntity<Void> deductStock(
            @PathVariable("id") UUID productId,
            @Valid @RequestBody DeductStockRequest request) {
        commerceService.deductStockAtomically(productId, request.getQuantity());
        return ResponseEntity.noContent().build();
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
