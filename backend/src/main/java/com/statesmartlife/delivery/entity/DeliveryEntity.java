package com.statesmartlife.delivery.entity;

import com.statesmartlife.delivery.dto.DeliveryFailureReason;
import com.statesmartlife.delivery.dto.DeliveryStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tbl_deliveries")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "order_id", nullable = false, unique = true)
    private UUID orderId;

    @Column(name = "delivery_partner_id")
    private UUID deliveryPartnerId;

    @Column(name = "pickup_address", nullable = false)
    private String pickupAddress;

    @Column(name = "dropoff_address", nullable = false)
    private String dropoffAddress;

    @Column(name = "pickup_address_line")
    private String pickupAddressLine;

    @Column(name = "pickup_city")
    private String pickupCity;

    @Column(name = "pickup_state")
    private String pickupState;

    @Column(name = "pickup_pincode")
    private String pickupPincode;

    @Column(name = "dropoff_address_line")
    private String dropoffAddressLine;

    @Column(name = "dropoff_city")
    private String dropoffCity;

    @Column(name = "dropoff_state")
    private String dropoffState;

    @Column(name = "dropoff_pincode")
    private String dropoffPincode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DeliveryStatus status;

    @Column(name = "delivery_fee", nullable = false)
    private BigDecimal deliveryFee;

    @Column(name = "estimated_arrival")
    private Instant estimatedArrival;

    @Column(name = "assigned_at")
    private Instant assignedAt;

    @Column(name = "picked_up_at")
    private Instant pickedUpAt;

    @Column(name = "in_transit_at")
    private Instant inTransitAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "failed_at")
    private Instant failedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "failure_reason")
    private DeliveryFailureReason failureReason;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
