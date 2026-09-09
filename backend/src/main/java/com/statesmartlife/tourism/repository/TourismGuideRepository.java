package com.statesmartlife.tourism.repository;

import com.statesmartlife.tourism.entity.TourismGuideEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TourismGuideRepository extends JpaRepository<TourismGuideEntity, UUID> {
    Optional<TourismGuideEntity> findByUserId(UUID userId);
    List<TourismGuideEntity> findByIsVerifiedTrueAndIsAvailableTrue();
    long countByIsVerifiedTrueAndIsAvailableTrue();
}
