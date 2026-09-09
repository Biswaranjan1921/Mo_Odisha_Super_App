-- V10__Emergency_Enhancements.sql

-- 1. Enhance tbl_emergency_requests with coordinates, address, response service, responder ID, timestamps and optimistic locking version
ALTER TABLE tbl_emergency_requests ADD COLUMN latitude NUMERIC(10,7) NOT NULL DEFAULT 20.2961;
ALTER TABLE tbl_emergency_requests ADD COLUMN longitude NUMERIC(10,7) NOT NULL DEFAULT 85.8245;
ALTER TABLE tbl_emergency_requests ADD COLUMN address_text TEXT;
ALTER TABLE tbl_emergency_requests ADD COLUMN response_service VARCHAR(50) NOT NULL DEFAULT 'AMBULANCE';
ALTER TABLE tbl_emergency_requests ADD COLUMN assigned_responder_id UUID REFERENCES tbl_auth_users(id) ON DELETE SET NULL;
ALTER TABLE tbl_emergency_requests ADD COLUMN dispatcher_notes TEXT;
ALTER TABLE tbl_emergency_requests ADD COLUMN dispatched_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE tbl_emergency_requests ADD COLUMN en_route_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE tbl_emergency_requests ADD COLUMN on_scene_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE tbl_emergency_requests ADD COLUMN cancelled_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE tbl_emergency_requests ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

-- 2. Performance Indexes
CREATE INDEX idx_emergency_status_reported ON tbl_emergency_requests(status, reported_at);
CREATE INDEX idx_emergency_requester_active ON tbl_emergency_requests(requester_id, status);
