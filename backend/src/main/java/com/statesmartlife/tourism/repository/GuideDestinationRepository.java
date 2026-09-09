package com.statesmartlife.tourism.repository;

import com.statesmartlife.tourism.entity.GuideDestinationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GuideDestinationRepository extends JpaRepository<GuideDestinationEntity, UUID> {
    List<GuideDestinationEntity> findByGuideId(UUID guideId);
    boolean existsByGuideIdAndTourismPlaceId(UUID guideId, UUID tourismPlaceId);
    void deleteByGuideId(UUID guideId);
}
