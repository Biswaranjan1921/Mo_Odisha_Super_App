-- =========================================================================
-- FLYWAY MIGRATION V6: COMMERCE ENHANCEMENTS & PERFORMANCE INDEXES
-- =========================================================================

-- Add latitude & longitude to stores if not present
ALTER TABLE tbl_shop_stores ADD COLUMN IF NOT EXISTS latitude DOUBLE PRECISION;
ALTER TABLE tbl_shop_stores ADD COLUMN IF NOT EXISTS longitude DOUBLE PRECISION;

-- Create performance indices
CREATE INDEX IF NOT EXISTS idx_shop_stores_category_active ON tbl_shop_stores(category, is_active);
CREATE INDEX IF NOT EXISTS idx_shop_products_store_price ON tbl_shop_products(store_id, price);
CREATE INDEX IF NOT EXISTS idx_shop_inventory_product_stock ON tbl_shop_inventory(product_id, stock_quantity);
