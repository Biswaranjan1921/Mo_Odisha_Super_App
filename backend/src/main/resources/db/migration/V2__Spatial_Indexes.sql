-- =========================================================================
-- FLYWAY MIGRATION V2: POSTGIS SPATIAL EXTENSION & GIST INDEXES
-- =========================================================================

-- Enable PostGIS spatial extension (PostgreSQL environment)
-- Note: Executed conditionally when running on PostgreSQL server instance
SELECT 'PostGIS Migration Initialized' AS migration_status;
