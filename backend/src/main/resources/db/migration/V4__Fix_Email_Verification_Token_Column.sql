-- =========================================================================
-- FLYWAY MIGRATION V4: REMOVE RAW VERIFICATION TOKEN CONSTRAINT
-- =========================================================================

ALTER TABLE tbl_email_verifications ALTER COLUMN verification_token DROP NOT NULL;
