package com.statesmartlife.payment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessPaymentRequest {

    @NotBlank(message = "Payment method is required")
    private String paymentMethod; // MOCK_UPI, MOCK_CARD, MOCK_NETBANKING
}
