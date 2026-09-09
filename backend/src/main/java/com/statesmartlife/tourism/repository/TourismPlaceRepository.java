package com.statesmartlife.tourism.repository;

import com.statesmartlife.tourism.entity.TourismPlaceEntity;
import com.statesmartlife.tourism.enums.TourismCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TourismPlaceRepository extends JpaRepository<TourismPlaceEntity, UUID> {
    List<TourismPlaceEntity> findByCategory(TourismCategory category);
    List<TourismPlaceEntity> findByNameContainingIgnoreCase(String keyword);
}
