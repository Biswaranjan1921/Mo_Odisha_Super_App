package com.statesmartlife.healthcare.service;

import com.statesmartlife.healthcare.dto.AppointmentResponse;
import com.statesmartlife.healthcare.dto.BookAppointmentRequest;
import com.statesmartlife.healthcare.dto.DoctorResponse;
import com.statesmartlife.healthcare.dto.DoctorScheduleResponse;
import com.statesmartlife.healthcare.dto.HospitalResponse;
import com.statesmartlife.healthcare.dto.UpdateAppointmentStatusRequest;
import com.statesmartlife.healthcare.dto.UpdatePrescriptionRequest;
import com.statesmartlife.healthcare.entity.AppointmentEntity;
import com.statesmartlife.healthcare.entity.DoctorEntity;
import com.statesmartlife.healthcare.entity.DoctorScheduleEntity;
import com.statesmartlife.healthcare.entity.HospitalEntity;
import com.statesmartlife.healthcare.enums.AppointmentStatus;
import com.statesmartlife.healthcare.exception.AppointmentSlotUnavailableException;
import com.statesmartlife.healthcare.exception.DoctorNotFoundException;
import com.statesmartlife.healthcare.exception.InvalidAppointmentTimeException;
import com.statesmartlife.healthcare.exception.InvalidAppointmentTransitionException;
import com.statesmartlife.healthcare.exception.UnauthorizedHealthcareAccessException;
import com.statesmartlife.healthcare.repository.AppointmentRepository;
import com.statesmartlife.healthcare.repository.DoctorRepository;
import com.statesmartlife.healthcare.repository.DoctorScheduleRepository;
import com.statesmartlife.healthcare.repository.HospitalRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class HealthcareServiceImpl implements HealthcareService {

    private final HospitalRepository hospitalRepository;
    private final DoctorRepository doctorRepository;
    private final DoctorScheduleRepository doctorScheduleRepository;
    private final AppointmentRepository appointmentRepository;

    public HealthcareServiceImpl(HospitalRepository hospitalRepository,
                                 DoctorRepository doctorRepository,
                                 DoctorScheduleRepository doctorScheduleRepository,
                                 AppointmentRepository appointmentRepository) {
        this.hospitalRepository = hospitalRepository;
        this.doctorRepository = doctorRepository;
        this.doctorScheduleRepository = doctorScheduleRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<HospitalResponse> getAllHospitals() {
        return hospitalRepository.findAll().stream()
                .map(this::mapToHospitalResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DoctorResponse> getDoctors(String specialization, UUID hospitalId, Pageable pageable) {
        Page<DoctorEntity> page;
        if (hospitalId != null && specialization != null && !specialization.isBlank()) {
            page = doctorRepository.findByHospitalIdAndSpecializationContainingIgnoreCaseAndIsAvailableTrue(hospitalId, specialization.trim(), pageable);
        } else if (hospitalId != null) {
            page = doctorRepository.findByHospitalIdAndIsAvailableTrue(hospitalId, pageable);
        } else if (specialization != null && !specialization.isBlank()) {
            page = doctorRepository.findBySpecializationContainingIgnoreCaseAndIsAvailableTrue(specialization.trim(), pageable);
        } else {
            page = doctorRepository.findByIsAvailableTrue(pageable);
        }
        return page.map(this::mapToDoctorResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public DoctorResponse getDoctorById(UUID doctorId) {
        DoctorEntity doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with ID: " + doctorId));
        return mapToDoctorResponse(doctor);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoctorScheduleResponse> getDoctorSchedules(UUID doctorId) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new DoctorNotFoundException("Doctor not found with ID: " + doctorId);
        }
        return doctorScheduleRepository.findByDoctorIdAndIsActiveTrue(doctorId).stream()
                .map(this::mapToScheduleResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AppointmentResponse bookAppointment(UUID currentUserId, BookAppointmentRequest request) {
        DoctorEntity doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with ID: " + request.getDoctorId()));

        // Validate schedule and working hours
        validateSchedule(doctor, request.getAppointmentTime());

        // Application-level double booking check
        if (appointmentRepository.existsByDoctorIdAndAppointmentTimeAndStatusIn(
                doctor.getId(),
                request.getAppointmentTime(),
                List.of(AppointmentStatus.BOOKED, AppointmentStatus.CONFIRMED))) {
            throw new AppointmentSlotUnavailableException("Appointment slot is already booked for this doctor at " + request.getAppointmentTime());
        }

        BigDecimal feeSnapshot = doctor.getConsultationFee() != null ? doctor.getConsultationFee() : BigDecimal.ZERO;

        AppointmentEntity appointment = AppointmentEntity.builder()
                .doctor(doctor)
                .patientId(currentUserId)
                .appointmentTime(request.getAppointmentTime())
                .status(AppointmentStatus.BOOKED)
                .symptomsDescription(request.getSymptomsDescription())
                .consultationFee(feeSnapshot)
                .build();

        try {
            AppointmentEntity saved = appointmentRepository.saveAndFlush(appointment);
            return mapToAppointmentResponse(saved);
        } catch (DataIntegrityViolationException ex) {
            throw new AppointmentSlotUnavailableException("Appointment slot was booked concurrently by another patient");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AppointmentResponse> getCitizenAppointments(UUID currentUserId, Pageable pageable) {
        return appointmentRepository.findByPatientIdOrderByAppointmentTimeDesc(currentUserId, pageable)
                .map(this::mapToAppointmentResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AppointmentResponse> getDoctorAppointments(UUID currentUserId, String userRole, Pageable pageable) {
        DoctorEntity doctor = getAuthorizedDoctorForUser(currentUserId, userRole);
        return appointmentRepository.findByDoctorIdOrderByAppointmentTimeDesc(doctor.getId(), pageable)
                .map(this::mapToAppointmentResponse);
    }

    @Override
    @Transactional
    public AppointmentResponse updateAppointmentStatus(UUID currentUserId, String userRole, UUID appointmentId, UpdateAppointmentStatusRequest request) {
        AppointmentEntity appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new InvalidAppointmentTransitionException("Appointment not found with ID: " + appointmentId));

        boolean isAdmin = userRole != null && userRole.toUpperCase().contains("ADMIN");
        boolean isPatient = appointment.getPatientId().equals(currentUserId);
        
        DoctorEntity doctor = appointment.getDoctor();
        boolean isAssignedDoctor = doctor.getUserId() != null && doctor.getUserId().equals(currentUserId);

        // Check permission: Patient can cancel, Doctor/Admin can confirm/cancel
        if (!isAdmin && !isPatient && !isAssignedDoctor) {
            throw new UnauthorizedHealthcareAccessException("User is not authorized to update status of appointment " + appointmentId);
        }

        // Validate state machine matrix transition
        validateTransition(appointment.getStatus(), request.getStatus());

        appointment.setStatus(request.getStatus());
        if (request.getStatus() == AppointmentStatus.COMPLETED) {
            appointment.setCompletedAt(Instant.now());
        }

        AppointmentEntity updated = appointmentRepository.save(appointment);
        return mapToAppointmentResponse(updated);
    }

    @Override
    @Transactional
    public AppointmentResponse issuePrescription(UUID currentUserId, String userRole, UUID appointmentId, UpdatePrescriptionRequest request) {
        AppointmentEntity appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new InvalidAppointmentTransitionException("Appointment not found with ID: " + appointmentId));

        DoctorEntity doctor = getAuthorizedDoctorForUser(currentUserId, userRole);

        // Ensure appointment belongs to this verified doctor
        if (!appointment.getDoctor().getId().equals(doctor.getId())) {
            throw new UnauthorizedHealthcareAccessException("Doctor is not assigned to appointment " + appointmentId);
        }

        // Must be in BOOKED or CONFIRMED state before prescription issuance
        if (appointment.getStatus() == AppointmentStatus.COMPLETED || appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new InvalidAppointmentTransitionException("Cannot issue prescription for terminal appointment status: " + appointment.getStatus());
        }

        appointment.setPrescriptionNotes(request.getPrescriptionNotes());
        appointment.setPrescriptionUrl(request.getPrescriptionUrl());
        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointment.setCompletedAt(Instant.now());

        AppointmentEntity updated = appointmentRepository.save(appointment);
        return mapToAppointmentResponse(updated);
    }

    private DoctorEntity getAuthorizedDoctorForUser(UUID userId, String role) {
        boolean isAdmin = role != null && role.toUpperCase().contains("ADMIN");
        DoctorEntity doctor = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new UnauthorizedHealthcareAccessException("No doctor profile associated with authenticated user account"));

        if (!isAdmin && !doctor.isVerified()) {
            throw new UnauthorizedHealthcareAccessException("Doctor profile is not verified for practice");
        }

        return doctor;
    }

    private void validateSchedule(DoctorEntity doctor, LocalDateTime appointmentTime) {
        if (!doctor.isAvailable()) {
            throw new InvalidAppointmentTimeException("Doctor is currently marked as unavailable");
        }

        if (appointmentTime.isBefore(LocalDateTime.now().minusMinutes(5))) {
            throw new InvalidAppointmentTimeException("Appointment time must be in the future");
        }

        String dayOfWeek = appointmentTime.getDayOfWeek().name();
        List<DoctorScheduleEntity> schedules = doctorScheduleRepository.findByDoctorIdAndDayOfWeekIgnoreCaseAndIsActiveTrue(doctor.getId(), dayOfWeek);

        if (!schedules.isEmpty()) {
            LocalTime requestedTime = appointmentTime.toLocalTime();
            boolean withinSlot = false;
            for (DoctorScheduleEntity schedule : schedules) {
                LocalTime start = schedule.getStartTime();
                LocalTime end = schedule.getEndTime();
                int duration = schedule.getSlotDurationMinutes();

                if (!requestedTime.isBefore(start) && requestedTime.isBefore(end)) {
                    long minutesFromStart = java.time.Duration.between(start, requestedTime).toMinutes();
                    if (minutesFromStart % duration == 0) {
                        withinSlot = true;
                        break;
                    }
                }
            }

            if (!withinSlot) {
                throw new InvalidAppointmentTimeException("Requested appointment time " + appointmentTime + " does not match doctor's active schedule slots for " + dayOfWeek);
            }
        }
    }

    private void validateTransition(AppointmentStatus current, AppointmentStatus target) {
        boolean valid = switch (current) {
            case BOOKED -> target == AppointmentStatus.CONFIRMED || target == AppointmentStatus.CANCELLED;
            case CONFIRMED -> target == AppointmentStatus.COMPLETED || target == AppointmentStatus.CANCELLED;
            case COMPLETED, CANCELLED -> false;
        };

        if (!valid) {
            throw new InvalidAppointmentTransitionException("Cannot change appointment status from " + current + " to " + target);
        }
    }

    private HospitalResponse mapToHospitalResponse(HospitalEntity hospital) {
        return HospitalResponse.builder()
                .id(hospital.getId())
                .name(hospital.getName())
                .address(hospital.getAddress())
                .phoneNumber(hospital.getPhoneNumber())
                .hasEmergencyService(hospital.isHasEmergencyService())
                .createdAt(hospital.getCreatedAt())
                .build();
    }

    private DoctorResponse mapToDoctorResponse(DoctorEntity doctor) {
        return DoctorResponse.builder()
                .id(doctor.getId())
                .hospitalId(doctor.getHospital() != null ? doctor.getHospital().getId() : null)
                .hospitalName(doctor.getHospital() != null ? doctor.getHospital().getName() : null)
                .userId(doctor.getUserId())
                .name(doctor.getName())
                .specialization(doctor.getSpecialization())
                .availabilitySchedule(doctor.getAvailabilitySchedule())
                .licenseNumber(doctor.getLicenseNumber())
                .consultationFee(doctor.getConsultationFee())
                .isAvailable(doctor.isAvailable())
                .isVerified(doctor.isVerified())
                .createdAt(doctor.getCreatedAt())
                .build();
    }

    private DoctorScheduleResponse mapToScheduleResponse(DoctorScheduleEntity schedule) {
        return DoctorScheduleResponse.builder()
                .id(schedule.getId())
                .doctorId(schedule.getDoctor().getId())
                .dayOfWeek(schedule.getDayOfWeek())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .slotDurationMinutes(schedule.getSlotDurationMinutes())
                .isActive(schedule.isActive())
                .build();
    }

    private AppointmentResponse mapToAppointmentResponse(AppointmentEntity appointment) {
        DoctorEntity d = appointment.getDoctor();
        return AppointmentResponse.builder()
                .id(appointment.getId())
                .doctorId(d.getId())
                .doctorName(d.getName())
                .doctorSpecialization(d.getSpecialization())
                .hospitalName(d.getHospital() != null ? d.getHospital().getName() : null)
                .patientId(appointment.getPatientId())
                .appointmentTime(appointment.getAppointmentTime())
                .status(appointment.getStatus())
                .symptomsDescription(appointment.getSymptomsDescription())
                .consultationFee(appointment.getConsultationFee())
                .prescriptionNotes(appointment.getPrescriptionNotes())
                .prescriptionUrl(appointment.getPrescriptionUrl())
                .completedAt(appointment.getCompletedAt())
                .createdAt(appointment.getCreatedAt())
                .build();
    }
}
