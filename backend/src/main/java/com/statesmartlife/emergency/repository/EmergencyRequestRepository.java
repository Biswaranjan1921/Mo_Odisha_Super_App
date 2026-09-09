package com.statesmartlife.emergency.repository;

import com.statesmartlife.emergency.entity.EmergencyRequestEntity;
import com.statesmartlife.emergency.enums.EmergencyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface EmergencyRequestRepository extends JpaRepository<EmergencyRequestEntity, UUID> {

    boolean existsByRequesterIdAndStatusIn(UUID requesterId, Collection<EmergencyStatus> statuses);

    Page<EmergencyRequestEntity> findByRequesterIdOrderByReportedAtDesc(UUID requesterId, Pageable pageable);

    List<EmergencyRequestEntity> findByRequesterIdOrderByReportedAtDesc(UUID requesterId);

    Page<EmergencyRequestEntity> findByStatusInOrderByReportedAtDesc(Collection<EmergencyStatus> statuses, Pageable pageable);

    Page<EmergencyRequestEntity> findAllByOrderByReportedAtDesc(Pageable pageable);

    long countByStatusIn(Collection<EmergencyStatus> statuses);
}
