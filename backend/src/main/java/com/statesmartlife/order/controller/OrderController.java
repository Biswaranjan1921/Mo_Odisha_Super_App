package com.statesmartlife.order.controller;

import com.statesmartlife.common.config.OpenApiConfig;
import com.statesmartlife.common.exception.BusinessRuleException;
import com.statesmartlife.order.dto.CreateOrderRequest;
import com.statesmartlife.order.dto.OrderResponse;
import com.statesmartlife.order.service.OrderService;
import com.statesmartlife.payment.dto.ProcessPaymentRequest;
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

@Tag(name = "Commerce - Orders & Payments", description = "Order placement, 2-step payment execution, and order history APIs")
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
@RestController
@RequestMapping("/orders")
@PreAuthorize("isAuthenticated()")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @Operation(summary = "Place order", description = "Places an order with backend-authoritative pricing, delivery address snapshotting, and atomic stock reservation.")
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        UUID customerId = extractAuthenticatedUserId();
        OrderResponse response = orderService.createOrder(customerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Process order payment", description = "Executes payment for a PENDING_PAYMENT order via mock payment provider (idempotent for PAID orders).")
    @PostMapping("/{id}/pay")
    public ResponseEntity<OrderResponse> processPayment(
            @PathVariable("id") UUID orderId,
            @Valid @RequestBody ProcessPaymentRequest request) {
        UUID customerId = extractAuthenticatedUserId();
        OrderResponse response = orderService.processPayment(customerId, orderId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get user order history", description = "Retrieves paginated order history for the authenticated citizen.")
    @GetMapping("/me")
    public ResponseEntity<Page<OrderResponse>> getMyOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        UUID customerId = extractAuthenticatedUserId();
        Pageable pageable = PageRequest.of(page, size);
        Page<OrderResponse> response = orderService.getOrdersForCustomer(customerId, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get order details", description = "Retrieves details of a specific order belonging to the authenticated citizen.")
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable("id") UUID id) {
        UUID customerId = extractAuthenticatedUserId();
        OrderResponse response = orderService.getOrderDetails(customerId, id);
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
}
