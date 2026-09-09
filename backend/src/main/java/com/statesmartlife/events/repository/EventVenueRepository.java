package com.statesmartlife.events.repository;

import com.statesmartlife.events.entity.EventVenueEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EventVenueRepository extends JpaRepository<EventVenueEntity, UUID> {
    List<EventVenueEntity> findByIsActiveTrue();
    List<EventVenueEntity> findByNameContainingIgnoreCaseAndIsActiveTrue(String keyword);
}
