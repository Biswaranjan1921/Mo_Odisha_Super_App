package com.statesmartlife.healthcare.repository;

import com.statesmartlife.healthcare.entity.AppointmentEntity;
import com.statesmartlife.healthcare.enums.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<AppointmentEntity, UUID> {

    boolean existsByDoctorIdAndAppointmentTimeAndStatusIn(UUID doctorId, LocalDateTime appointmentTime, Collection<AppointmentStatus> statuses);

    Page<AppointmentEntity> findByPatientIdOrderByAppointmentTimeDesc(UUID patientId, Pageable pageable);

    Page<AppointmentEntity> findByDoctorIdOrderByAppointmentTimeDesc(UUID doctorId, Pageable pageable);

    long countByStatusIn(Collection<AppointmentStatus> statuses);
}
