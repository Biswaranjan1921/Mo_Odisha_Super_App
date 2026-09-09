package com.statesmartlife.governance.repository;

import com.statesmartlife.governance.entity.DistrictAnalyticsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DistrictAnalyticsRepository extends JpaRepository<DistrictAnalyticsEntity, UUID> {
    Optional<DistrictAnalyticsEntity> findByDistrictId(UUID districtId);
}
