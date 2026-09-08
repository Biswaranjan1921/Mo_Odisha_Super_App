-- =========================================================================
-- FLYWAY MIGRATION V5: USER PROFILE INDEXES & PERFORMANCE OPTIMIZATION
-- =========================================================================

CREATE INDEX IF NOT EXISTS idx_user_profiles_user_id ON tbl_user_profiles(user_id);
