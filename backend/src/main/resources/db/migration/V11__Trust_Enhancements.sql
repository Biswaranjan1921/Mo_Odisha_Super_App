-- V11__Trust_Enhancements.sql

ALTER TABLE tbl_trust_scores ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

CREATE TABLE IF NOT EXISTS tbl_incident_tickets (
    id UUID PRIMARY KEY,
    reporter_id UUID NOT NULL REFERENCES tbl_auth_users(id) ON DELETE CASCADE,
    target_entity_id UUID NOT NULL,
    target_entity_type VARCHAR(50) NOT NULL,
    subject VARCHAR(150) NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'OPEN',
    resolution_notes TEXT,
    resolved_by UUID REFERENCES tbl_auth_users(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_ticket_entity_type CHECK (target_entity_type IN ('STORE', 'DELIVERY_PARTNER', 'DOCTOR')),
    CONSTRAINT chk_ticket_status CHECK (status IN ('OPEN', 'UNDER_REVIEW', 'RESOLVED', 'REJECTED'))
);

CREATE INDEX IF NOT EXISTS idx_trust_scores_entity ON tbl_trust_scores (entity_id, entity_type);
CREATE INDEX IF NOT EXISTS idx_incident_tickets_reporter ON tbl_incident_tickets (reporter_id, status);
