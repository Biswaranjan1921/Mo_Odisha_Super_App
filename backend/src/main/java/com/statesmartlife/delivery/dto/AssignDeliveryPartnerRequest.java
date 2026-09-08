package com.statesmartlife.delivery.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignDeliveryPartnerRequest {

    @NotNull(message = "Delivery partner ID is required")
    private UUID deliveryPartnerId;
}
