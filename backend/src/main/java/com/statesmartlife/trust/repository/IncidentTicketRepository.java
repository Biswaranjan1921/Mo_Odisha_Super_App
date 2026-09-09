package com.statesmartlife.trust.repository;

import com.statesmartlife.trust.entity.IncidentTicketEntity;
import com.statesmartlife.trust.enums.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IncidentTicketRepository extends JpaRepository<IncidentTicketEntity, UUID> {
    List<IncidentTicketEntity> findByReporterIdOrderByCreatedAtDesc(UUID reporterId);
    List<IncidentTicketEntity> findAllByOrderByCreatedAtDesc();
    long countByStatus(TicketStatus status);
}
