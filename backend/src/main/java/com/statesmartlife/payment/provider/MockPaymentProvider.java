package com.statesmartlife.payment.provider;

import com.statesmartlife.payment.dto.PaymentResponse;
import com.statesmartlife.payment.dto.PaymentStatus;
import com.statesmartlife.payment.entity.PaymentEntity;
import com.statesmartlife.payment.repository.PaymentRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Component
public class MockPaymentProvider implements PaymentProvider {

    private final PaymentRepository paymentRepository;

    public MockPaymentProvider(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Override
    public PaymentResponse processPayment(UUID orderId, String paymentMethod, BigDecimal amount) {
        String method = paymentMethod != null ? paymentMethod.toUpperCase() : "MOCK_UPI";
        String transactionId = "MOCK-" + method + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        PaymentEntity payment = PaymentEntity.builder()
                .orderId(orderId)
                .paymentProvider("MOCK_PROVIDER")
                .paymentMethod(method)
                .transactionId(transactionId)
                .amount(amount)
                .paymentStatus(PaymentStatus.SUCCESS)
                .build();

        PaymentEntity saved = paymentRepository.save(payment);

        return PaymentResponse.builder()
                .id(saved.getId())
                .orderId(saved.getOrderId())
                .paymentProvider(saved.getPaymentProvider())
                .paymentMethod(saved.getPaymentMethod())
                .transactionId(saved.getTransactionId())
                .amount(saved.getAmount())
                .paymentStatus(saved.getPaymentStatus())
                .createdAt(saved.getCreatedAt() != null ? saved.getCreatedAt() : Instant.now())
                .build();
    }
}
