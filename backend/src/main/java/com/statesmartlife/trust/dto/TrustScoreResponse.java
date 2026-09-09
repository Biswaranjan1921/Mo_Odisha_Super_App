package com.statesmartlife.trust.dto;

import com.statesmartlife.trust.enums.BadgeLevel;
import com.statesmartlife.trust.enums.EntityType;
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
public class TrustScoreResponse {
    private UUID id;
    private UUID entityId;
    private EntityType entityType;
    private int verifiedTransactionsCount;
    private int repeatUsersCount;
    private int complaintsCount;
    private BigDecimal trustScore;
    private BadgeLevel badgeLevel;
    private Instant updatedAt;
}
