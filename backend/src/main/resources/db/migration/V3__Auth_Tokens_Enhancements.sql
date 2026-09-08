-- =========================================================================
-- FLYWAY MIGRATION V3: AUTH & VERIFICATION TOKEN ENHANCEMENTS
-- =========================================================================

-- Add token hash column and used status flag for secure hashed token storage
ALTER TABLE tbl_email_verifications ADD COLUMN IF NOT EXISTS verification_token_hash VARCHAR(255);
ALTER TABLE tbl_email_verifications ADD COLUMN IF NOT EXISTS used BOOLEAN DEFAULT FALSE;

-- Ensure index on token hash
CREATE INDEX IF NOT EXISTS idx_email_verifications_token_hash ON tbl_email_verifications(verification_token_hash);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_hash ON tbl_refresh_tokens(token_hash);
