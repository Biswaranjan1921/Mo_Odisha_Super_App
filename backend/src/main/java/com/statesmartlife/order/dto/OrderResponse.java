package com.statesmartlife.order.dto;

import com.statesmartlife.payment.dto.PaymentResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private UUID id;
    private UUID customerId;
    private UUID storeId;
    private String storeName;
    private OrderStatus status;
    private BigDecimal subtotal;
    private BigDecimal deliveryFee;
    private BigDecimal totalAmount;
    private DeliveryAddressDto deliveryAddress;
    private List<OrderItemResponse> items;
    private PaymentResponse payment;
    private Instant createdAt;
}
