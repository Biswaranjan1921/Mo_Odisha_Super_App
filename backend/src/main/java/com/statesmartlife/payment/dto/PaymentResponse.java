package com.statesmartlife.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private UUID id;
    private UUID orderId;
    private String paymentProvider;
    private String paymentMethod;
    private String transactionId;
    private BigDecimal amount;
    private PaymentStatus paymentStatus;
    private Instant createdAt;
}
