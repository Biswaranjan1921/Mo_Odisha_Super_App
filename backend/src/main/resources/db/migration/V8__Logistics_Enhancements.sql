-- =========================================================================
-- FLYWAY MIGRATION V8: LOGISTICS & DELIVERY LIFECYCLE ENHANCEMENTS
-- =========================================================================

-- Dual address historical snapshotting columns on tbl_deliveries
ALTER TABLE tbl_deliveries ADD COLUMN IF NOT EXISTS pickup_address_line TEXT;
ALTER TABLE tbl_deliveries ADD COLUMN IF NOT EXISTS pickup_city VARCHAR(100);
ALTER TABLE tbl_deliveries ADD COLUMN IF NOT EXISTS pickup_state VARCHAR(100);
ALTER TABLE tbl_deliveries ADD COLUMN IF NOT EXISTS pickup_pincode VARCHAR(15);

ALTER TABLE tbl_deliveries ADD COLUMN IF NOT EXISTS dropoff_address_line TEXT;
ALTER TABLE tbl_deliveries ADD COLUMN IF NOT EXISTS dropoff_city VARCHAR(100);
ALTER TABLE tbl_deliveries ADD COLUMN IF NOT EXISTS dropoff_state VARCHAR(100);
ALTER TABLE tbl_deliveries ADD COLUMN IF NOT EXISTS dropoff_pincode VARCHAR(15);

-- Lifecycle timestamps & failure reason
ALTER TABLE tbl_deliveries ADD COLUMN IF NOT EXISTS assigned_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE tbl_deliveries ADD COLUMN IF NOT EXISTS picked_up_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE tbl_deliveries ADD COLUMN IF NOT EXISTS in_transit_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE tbl_deliveries ADD COLUMN IF NOT EXISTS failed_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE tbl_deliveries ADD COLUMN IF NOT EXISTS failure_reason VARCHAR(255);

-- Performance & Uniqueness indexes (1 order = 1 delivery)
CREATE UNIQUE INDEX IF NOT EXISTS idx_deliveries_order_id_unique ON tbl_deliveries(order_id);
CREATE INDEX IF NOT EXISTS idx_deliveries_partner_status ON tbl_deliveries(delivery_partner_id, status);
