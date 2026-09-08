package com.statesmartlife.delivery.dto;

import com.statesmartlife.order.dto.DeliveryAddressDto;
import com.statesmartlife.order.dto.OrderItemResponse;
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
public class DeliveryResponse {
    private UUID id;
    private UUID orderId;
    private UUID deliveryPartnerId;
    private String deliveryPartnerName;
    private PickupAddressDto pickupAddress;
    private DeliveryAddressDto dropoffAddress;
    private DeliveryStatus status;
    private BigDecimal deliveryFee;
    private Instant estimatedArrival;
    private Instant assignedAt;
    private Instant pickedUpAt;
    private Instant inTransitAt;
    private Instant completedAt;
    private Instant failedAt;
    private DeliveryFailureReason failureReason;
    private String failureNotes;
    private List<OrderItemResponse> items;
    private Instant createdAt;
}
