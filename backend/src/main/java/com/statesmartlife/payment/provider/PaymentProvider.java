package com.statesmartlife.payment.provider;

import com.statesmartlife.payment.dto.PaymentResponse;

import java.math.BigDecimal;
import java.util.UUID;

public interface PaymentProvider {
    PaymentResponse processPayment(UUID orderId, String paymentMethod, BigDecimal amount);
}
