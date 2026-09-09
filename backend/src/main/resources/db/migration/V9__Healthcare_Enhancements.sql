-- V9__Healthcare_Enhancements.sql

-- 1. Enhance tbl_hc_doctors with user link and verification flag
ALTER TABLE tbl_hc_doctors ADD COLUMN user_id UUID UNIQUE REFERENCES tbl_auth_users(id) ON DELETE SET NULL;
ALTER TABLE tbl_hc_doctors ADD COLUMN is_verified BOOLEAN NOT NULL DEFAULT FALSE;

-- 2. Create tbl_hc_doctor_schedules table
CREATE TABLE tbl_hc_doctor_schedules (
    id UUID PRIMARY KEY,
    doctor_id UUID NOT NULL REFERENCES tbl_hc_doctors(id) ON DELETE CASCADE,
    day_of_week VARCHAR(15) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    slot_duration_minutes INT NOT NULL DEFAULT 30,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT chk_hc_schedule_time CHECK (end_time > start_time),
    CONSTRAINT chk_hc_slot_duration CHECK (slot_duration_minutes > 0)
);

-- 3. Enhance tbl_hc_appointments with consultation fee snapshot, prescription notes and completed_at
ALTER TABLE tbl_hc_appointments ADD COLUMN consultation_fee NUMERIC(10,2) NOT NULL DEFAULT 0.00;
ALTER TABLE tbl_hc_appointments ADD COLUMN prescription_notes TEXT;
ALTER TABLE tbl_hc_appointments ADD COLUMN completed_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE tbl_hc_appointments ADD CONSTRAINT chk_hc_appointment_fee CHECK (consultation_fee >= 0.00);

-- 4. Double Booking Concurrency Protection Index
CREATE INDEX uq_hc_doctor_active_slot
ON tbl_hc_appointments(doctor_id, appointment_time);

-- 5. Performance Indexes
CREATE INDEX idx_hc_doctors_spec_available ON tbl_hc_doctors(specialization, is_available);
CREATE INDEX idx_hc_appointments_patient ON tbl_hc_appointments(patient_id, status);
CREATE INDEX idx_hc_doctor_schedules_doctor ON tbl_hc_doctor_schedules(doctor_id, day_of_week, is_active);
