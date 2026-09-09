package com.statesmartlife.healthcare.service;

import com.statesmartlife.healthcare.dto.AppointmentResponse;
import com.statesmartlife.healthcare.dto.BookAppointmentRequest;
import com.statesmartlife.healthcare.dto.DoctorResponse;
import com.statesmartlife.healthcare.dto.DoctorScheduleResponse;
import com.statesmartlife.healthcare.dto.HospitalResponse;
import com.statesmartlife.healthcare.dto.UpdateAppointmentStatusRequest;
import com.statesmartlife.healthcare.dto.UpdatePrescriptionRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface HealthcareService {

    List<HospitalResponse> getAllHospitals();

    Page<DoctorResponse> getDoctors(String specialization, UUID hospitalId, Pageable pageable);

    DoctorResponse getDoctorById(UUID doctorId);

    List<DoctorScheduleResponse> getDoctorSchedules(UUID doctorId);

    AppointmentResponse bookAppointment(UUID currentUserId, BookAppointmentRequest request);

    Page<AppointmentResponse> getCitizenAppointments(UUID currentUserId, Pageable pageable);

    Page<AppointmentResponse> getDoctorAppointments(UUID currentUserId, String userRole, Pageable pageable);

    AppointmentResponse updateAppointmentStatus(UUID currentUserId, String userRole, UUID appointmentId, UpdateAppointmentStatusRequest request);

    AppointmentResponse issuePrescription(UUID currentUserId, String userRole, UUID appointmentId, UpdatePrescriptionRequest request);
}
