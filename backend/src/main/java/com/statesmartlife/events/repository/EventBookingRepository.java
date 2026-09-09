package com.statesmartlife.events.repository;

import com.statesmartlife.events.entity.EventBookingEntity;
import com.statesmartlife.events.enums.EventBookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface EventBookingRepository extends JpaRepository<EventBookingEntity, UUID> {
    List<EventBookingEntity> findByPlannerIdOrderByEventDateDesc(UUID plannerId);
    List<EventBookingEntity> findByVenueIdOrderByEventDateDesc(UUID venueId);
    boolean existsByVenueIdAndEventDateAndStatusIn(UUID venueId, LocalDate eventDate, Collection<EventBookingStatus> statuses);
}
