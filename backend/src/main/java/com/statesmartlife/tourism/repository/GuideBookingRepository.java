package com.statesmartlife.tourism.repository;

import com.statesmartlife.tourism.entity.GuideBookingEntity;
import com.statesmartlife.tourism.enums.GuideBookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface GuideBookingRepository extends JpaRepository<GuideBookingEntity, UUID> {
    List<GuideBookingEntity> findByTouristIdOrderByBookingDateDesc(UUID touristId);
    List<GuideBookingEntity> findByGuideIdOrderByBookingDateDesc(UUID guideId);
    boolean existsByGuideIdAndBookingDateAndStatusIn(UUID guideId, LocalDate bookingDate, Collection<GuideBookingStatus> statuses);
}
