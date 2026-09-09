package com.statesmartlife.healthcare.repository;

import com.statesmartlife.healthcare.entity.DoctorEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DoctorRepository extends JpaRepository<DoctorEntity, UUID> {

    Page<DoctorEntity> findByIsAvailableTrue(Pageable pageable);

    Page<DoctorEntity> findBySpecializationContainingIgnoreCaseAndIsAvailableTrue(String specialization, Pageable pageable);

    Page<DoctorEntity> findByHospitalIdAndIsAvailableTrue(UUID hospitalId, Pageable pageable);

    Page<DoctorEntity> findByHospitalIdAndSpecializationContainingIgnoreCaseAndIsAvailableTrue(UUID hospitalId, String specialization, Pageable pageable);

    Optional<DoctorEntity> findByUserId(UUID userId);
}
