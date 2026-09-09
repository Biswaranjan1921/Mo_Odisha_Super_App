package com.statesmartlife.trust.repository;

import com.statesmartlife.trust.entity.TrustScoreEntity;
import com.statesmartlife.trust.enums.EntityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TrustScoreRepository extends JpaRepository<TrustScoreEntity, UUID> {
    Optional<TrustScoreEntity> findByEntityIdAndEntityType(UUID entityId, EntityType entityType);
}
