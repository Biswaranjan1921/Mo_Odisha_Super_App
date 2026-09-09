package com.statesmartlife.tourism.repository;

import com.statesmartlife.tourism.entity.TourismGuideApplicationEntity;
import com.statesmartlife.tourism.enums.GuideApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TourismGuideApplicationRepository extends JpaRepository<TourismGuideApplicationEntity, UUID> {
    Optional<TourismGuideApplicationEntity> findByUserId(UUID userId);
    List<TourismGuideApplicationEntity> findByStatusOrderByCreatedAtDesc(GuideApplicationStatus status);
}
