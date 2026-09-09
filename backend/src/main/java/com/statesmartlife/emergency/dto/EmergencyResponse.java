package com.statesmartlife.emergency.dto;

import com.statesmartlife.emergency.enums.EmergencyStatus;
import com.statesmartlife.emergency.enums.EmergencyType;
import com.statesmartlife.emergency.enums.ResponseServiceType;
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
public class EmergencyResponse {

    private UUID id;
    private UUID requesterId;
    private String requesterPhone;
    private EmergencyType emergencyType;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String addressText;
    private EmergencyStatus status;
    private ResponseServiceType responseService;
    private UUID assignedResponderId;
    private String dispatcherNotes;
    private Instant reportedAt;
    private Instant dispatchedAt;
    private Instant enRouteAt;
    private Instant onSceneAt;
    private Instant resolvedAt;
    private Instant cancelledAt;
    private Instant updatedAt;
    private Long version;
}
