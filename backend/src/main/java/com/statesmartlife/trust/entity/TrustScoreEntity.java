package com.statesmartlife.trust.entity;

import com.statesmartlife.trust.enums.BadgeLevel;
import com.statesmartlife.trust.enums.EntityType;
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
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tbl_trust_scores")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrustScoreEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false, length = 50)
    private EntityType entityType;

    @Column(name = "verified_transactions_count", nullable = false)
    @Builder.Default
    private int verifiedTransactionsCount = 0;

    @Column(name = "repeat_users_count", nullable = false)
    @Builder.Default
    private int repeatUsersCount = 0;

    @Column(name = "complaints_count", nullable = false)
    @Builder.Default
    private int complaintsCount = 0;

    @Column(name = "trust_score", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal trustScore = new BigDecimal("100.00");

    @Enumerated(EnumType.STRING)
    @Column(name = "badge_level", nullable = false, length = 20)
    @Builder.Default
    private BadgeLevel badgeLevel = BadgeLevel.PLATINUM;

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
