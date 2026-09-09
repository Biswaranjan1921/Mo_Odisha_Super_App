package com.statesmartlife.healthcare.repository;

import com.statesmartlife.healthcare.entity.DoctorScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DoctorScheduleRepository extends JpaRepository<DoctorScheduleEntity, UUID> {

    List<DoctorScheduleEntity> findByDoctorIdAndIsActiveTrue(UUID doctorId);

    List<DoctorScheduleEntity> findByDoctorIdAndDayOfWeekIgnoreCaseAndIsActiveTrue(UUID doctorId, String dayOfWeek);
}
