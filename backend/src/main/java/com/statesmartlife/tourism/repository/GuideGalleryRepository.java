package com.statesmartlife.tourism.repository;

import com.statesmartlife.tourism.entity.GuideGalleryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GuideGalleryRepository extends JpaRepository<GuideGalleryEntity, UUID> {
    List<GuideGalleryEntity> findByGuideIdOrderByDisplayOrderAsc(UUID guideId);
}
