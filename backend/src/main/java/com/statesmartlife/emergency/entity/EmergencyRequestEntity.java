package com.statesmartlife.emergency.entity;

import com.statesmartlife.emergency.enums.EmergencyStatus;
import com.statesmartlife.emergency.enums.EmergencyType;
import com.statesmartlife.emergency.enums.ResponseServiceType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
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
@Table(name = "tbl_emergency_requests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyRequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "requester_id")
    private UUID requesterId;

    @Column(name = "requester_phone", nullable = false, length = 15)
    private String requesterPhone;

    @Enumerated(EnumType.STRING)
    @Column(name = "emergency_type", nullable = false, length = 50)
    private EmergencyType emergencyType;

    @Column(name = "latitude", nullable = false, precision = 10, scale = 7)
    @Builder.Default
    private BigDecimal latitude = new BigDecimal("20.2961000");

    @Column(name = "longitude", nullable = false, precision = 10, scale = 7)
    @Builder.Default
    private BigDecimal longitude = new BigDecimal("85.8245000");

    @Column(name = "address_text", columnDefinition = "TEXT")
    private String addressText;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    @Builder.Default
    private EmergencyStatus status = EmergencyStatus.REPORTED;

    @Enumerated(EnumType.STRING)
    @Column(name = "response_service", nullable = false, length = 50)
    @Builder.Default
    private ResponseServiceType responseService = ResponseServiceType.AMBULANCE;

    @Column(name = "assigned_ambulance_id")
    private UUID assignedAmbulanceId;

    @Column(name = "assigned_responder_id")
    private UUID assignedResponderId;

    @Column(name = "dispatcher_notes", columnDefinition = "TEXT")
    private String dispatcherNotes;

    @CreationTimestamp
    @Column(name = "reported_at", updatable = false)
    private Instant reportedAt;

    @Column(name = "dispatched_at")
    private Instant dispatchedAt;

    @Column(name = "en_route_at")
    private Instant enRouteAt;

    @Column(name = "on_scene_at")
    private Instant onSceneAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L;
}
