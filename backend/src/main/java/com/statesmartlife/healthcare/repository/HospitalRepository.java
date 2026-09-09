package com.statesmartlife.healthcare.repository;

import com.statesmartlife.healthcare.entity.HospitalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface HospitalRepository extends JpaRepository<HospitalEntity, UUID> {
}
