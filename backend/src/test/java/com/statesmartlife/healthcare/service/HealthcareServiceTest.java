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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class HealthcareServiceTest {

    private HospitalRepository hospitalRepository;
    private DoctorRepository doctorRepository;
    private DoctorScheduleRepository doctorScheduleRepository;
    private AppointmentRepository appointmentRepository;
    private HealthcareService healthcareService;

    private UUID hospitalId;
    private UUID doctorId;
    private UUID doctorUserId;
    private UUID patientId;
    private UUID appointmentId;

    private HospitalEntity hospital;
    private DoctorEntity doctor;
    private DoctorScheduleEntity schedule;

    @BeforeEach
    void setUp() {
        hospitalRepository = mock(HospitalRepository.class);
        doctorRepository = mock(DoctorRepository.class);
        doctorScheduleRepository = mock(DoctorScheduleRepository.class);
        appointmentRepository = mock(AppointmentRepository.class);

        healthcareService = new HealthcareServiceImpl(
                hospitalRepository,
                doctorRepository,
                doctorScheduleRepository,
                appointmentRepository
        );

        hospitalId = UUID.randomUUID();
        doctorId = UUID.randomUUID();
        doctorUserId = UUID.randomUUID();
        patientId = UUID.randomUUID();
        appointmentId = UUID.randomUUID();

        hospital = HospitalEntity.builder()
                .id(hospitalId)
                .name("AIIMS Bhubaneswar")
                .address("Sijua, Patrapada, Bhubaneswar")
                .phoneNumber("06742476789")
                .hasEmergencyService(true)
                .createdAt(Instant.now())
                .build();

        doctor = DoctorEntity.builder()
                .id(doctorId)
                .hospital(hospital)
                .userId(doctorUserId)
                .name("Dr. Sambit Mohanty")
                .specialization("Cardiology")
                .licenseNumber("OD-MED-998877")
                .consultationFee(new BigDecimal("500.00"))
                .isAvailable(true)
                .isVerified(true)
                .createdAt(Instant.now())
                .build();

        schedule = DoctorScheduleEntity.builder()
                .id(UUID.randomUUID())
                .doctor(doctor)
                .dayOfWeek("MONDAY")
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(14, 0))
                .slotDurationMinutes(30)
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("getAllHospitals returns list of hospital responses")
    void getAllHospitals_Success() {
        when(hospitalRepository.findAll()).thenReturn(List.of(hospital));

        List<HospitalResponse> result = healthcareService.getAllHospitals();

        assertEquals(1, result.size());
        assertEquals("AIIMS Bhubaneswar", result.get(0).getName());
    }

    @Test
    @DisplayName("getDoctorById returns doctor response when found")
    void getDoctorById_Success() {
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));

        DoctorResponse response = healthcareService.getDoctorById(doctorId);

        assertNotNull(response);
        assertEquals("Dr. Sambit Mohanty", response.getName());
        assertEquals(new BigDecimal("500.00"), response.getConsultationFee());
    }

    @Test
    @DisplayName("getDoctorById throws DoctorNotFoundException when missing")
    void getDoctorById_NotFound() {
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.empty());

        assertThrows(DoctorNotFoundException.class, () -> healthcareService.getDoctorById(doctorId));
    }

    @Test
    @DisplayName("bookAppointment snapshots consultation fee and creates BOOKED appointment")
    void bookAppointment_Success() {
        LocalDateTime futureSlot = LocalDateTime.now().plusDays(7).withHour(10).withMinute(0).withSecond(0).withNano(0);
        BookAppointmentRequest request = BookAppointmentRequest.builder()
                .doctorId(doctorId)
                .appointmentTime(futureSlot)
                .symptomsDescription("Chest pain and fatigue")
                .build();

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));

        AppointmentEntity savedAppointment = AppointmentEntity.builder()
                .id(appointmentId)
                .doctor(doctor)
                .patientId(patientId)
                .appointmentTime(futureSlot)
                .status(AppointmentStatus.BOOKED)
                .symptomsDescription("Chest pain and fatigue")
                .consultationFee(new BigDecimal("500.00"))
                .createdAt(Instant.now())
                .build();

        when(appointmentRepository.saveAndFlush(any(AppointmentEntity.class))).thenReturn(savedAppointment);

        AppointmentResponse response = healthcareService.bookAppointment(patientId, request);

        assertNotNull(response);
        assertEquals(AppointmentStatus.BOOKED, response.getStatus());
        assertEquals(patientId, response.getPatientId());
        assertEquals(new BigDecimal("500.00"), response.getConsultationFee());
    }

    @Test
    @DisplayName("bookAppointment throws AppointmentSlotUnavailableException (409) when slot occupied")
    void bookAppointment_SlotOccupied_Throws409() {
        LocalDateTime slot = LocalDateTime.now().plusDays(2).withHour(11).withMinute(0);
        BookAppointmentRequest request = BookAppointmentRequest.builder()
                .doctorId(doctorId)
                .appointmentTime(slot)
                .build();

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(appointmentRepository.existsByDoctorIdAndAppointmentTimeAndStatusIn(eq(doctorId), eq(slot), any()))
                .thenReturn(true);

        assertThrows(AppointmentSlotUnavailableException.class, () -> healthcareService.bookAppointment(patientId, request));
    }

    @Test
    @DisplayName("bookAppointment catches DataIntegrityViolationException and throws AppointmentSlotUnavailableException (409)")
    void bookAppointment_ConcurrentRace_Throws409() {
        LocalDateTime slot = LocalDateTime.now().plusDays(2).withHour(11).withMinute(0);
        BookAppointmentRequest request = BookAppointmentRequest.builder()
                .doctorId(doctorId)
                .appointmentTime(slot)
                .build();

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(appointmentRepository.existsByDoctorIdAndAppointmentTimeAndStatusIn(eq(doctorId), eq(slot), any()))
                .thenReturn(false);
        when(appointmentRepository.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("Unique index violation"));

        assertThrows(AppointmentSlotUnavailableException.class, () -> healthcareService.bookAppointment(patientId, request));
    }

    @Test
    @DisplayName("bookAppointment rejects appointment times outside active schedule slots")
    void bookAppointment_InvalidScheduleTime_Throws400() {
        LocalDateTime slotOnMonday = LocalDateTime.now().plusDays(7).withHour(10).withMinute(15); // 15 mins offset, schedule is 30 mins slot
        BookAppointmentRequest request = BookAppointmentRequest.builder()
                .doctorId(doctorId)
                .appointmentTime(slotOnMonday)
                .build();

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(doctorScheduleRepository.findByDoctorIdAndDayOfWeekIgnoreCaseAndIsActiveTrue(eq(doctorId), anyString()))
                .thenReturn(List.of(schedule));

        assertThrows(InvalidAppointmentTimeException.class, () -> healthcareService.bookAppointment(patientId, request));
    }

    @Test
    @DisplayName("updateAppointmentStatus validates state transitions correctly")
    void updateAppointmentStatus_Transitions() {
        AppointmentEntity appointment = AppointmentEntity.builder()
                .id(appointmentId)
                .doctor(doctor)
                .patientId(patientId)
                .appointmentTime(LocalDateTime.now().plusDays(1))
                .status(AppointmentStatus.BOOKED)
                .build();

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // BOOKED -> CONFIRMED (Valid)
        UpdateAppointmentStatusRequest confirmReq = new UpdateAppointmentStatusRequest(AppointmentStatus.CONFIRMED);
        AppointmentResponse res1 = healthcareService.updateAppointmentStatus(patientId, "ROLE_CUSTOMER", appointmentId, confirmReq);
        assertEquals(AppointmentStatus.CONFIRMED, res1.getStatus());

        // CONFIRMED -> BOOKED (Invalid)
        UpdateAppointmentStatusRequest invalidReq = new UpdateAppointmentStatusRequest(AppointmentStatus.BOOKED);
        assertThrows(InvalidAppointmentTransitionException.class,
                () -> healthcareService.updateAppointmentStatus(patientId, "ROLE_CUSTOMER", appointmentId, invalidReq));
    }

    @Test
    @DisplayName("issuePrescription by verified assigned doctor attaches notes and sets COMPLETED")
    void issuePrescription_VerifiedDoctor_Success() {
        AppointmentEntity appointment = AppointmentEntity.builder()
                .id(appointmentId)
                .doctor(doctor)
                .patientId(patientId)
                .appointmentTime(LocalDateTime.now().plusHours(2))
                .status(AppointmentStatus.CONFIRMED)
                .build();

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        when(doctorRepository.findByUserId(doctorUserId)).thenReturn(Optional.of(doctor));
        when(appointmentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        UpdatePrescriptionRequest req = UpdatePrescriptionRequest.builder()
                .prescriptionNotes("Take Paracetamol 500mg twice daily after meal.")
                .prescriptionUrl("https://storage.mo-odisha.gov.in/prescriptions/rx-1002.pdf")
                .build();

        AppointmentResponse res = healthcareService.issuePrescription(doctorUserId, "ROLE_HEALTHCARE_PROVIDER", appointmentId, req);

        assertNotNull(res);
        assertEquals(AppointmentStatus.COMPLETED, res.getStatus());
        assertEquals("Take Paracetamol 500mg twice daily after meal.", res.getPrescriptionNotes());
        assertNotNull(res.getCompletedAt());
    }

    @Test
    @DisplayName("issuePrescription by unverified doctor throws UnauthorizedHealthcareAccessException (403)")
    void issuePrescription_UnverifiedDoctor_Throws403() {
        DoctorEntity unverifiedDoctor = DoctorEntity.builder()
                .id(doctorId)
                .userId(doctorUserId)
                .isVerified(false)
                .build();

        AppointmentEntity appointment = AppointmentEntity.builder()
                .id(appointmentId)
                .doctor(unverifiedDoctor)
                .patientId(patientId)
                .status(AppointmentStatus.CONFIRMED)
                .build();

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        when(doctorRepository.findByUserId(doctorUserId)).thenReturn(Optional.of(unverifiedDoctor));

        UpdatePrescriptionRequest req = new UpdatePrescriptionRequest("Notes", "url");

        assertThrows(UnauthorizedHealthcareAccessException.class,
                () -> healthcareService.issuePrescription(doctorUserId, "ROLE_HEALTHCARE_PROVIDER", appointmentId, req));
    }
}
