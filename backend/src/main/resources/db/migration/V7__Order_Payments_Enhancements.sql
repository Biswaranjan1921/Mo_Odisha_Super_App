-- =========================================================================
-- FLYWAY MIGRATION V7: ORDERS, ORDER ITEMS & PAYMENTS ENHANCEMENTS
-- =========================================================================

-- Align tbl_shop_orders schema with delivery address snapshots & totals
ALTER TABLE tbl_shop_orders ADD COLUMN IF NOT EXISTS subtotal NUMERIC(10,2) DEFAULT 0.00 CHECK (subtotal >= 0.00);
ALTER TABLE tbl_shop_orders ADD COLUMN IF NOT EXISTS delivery_fee NUMERIC(10,2) DEFAULT 0.00 CHECK (delivery_fee >= 0.00);
ALTER TABLE tbl_shop_orders ADD COLUMN IF NOT EXISTS delivery_address_line TEXT;
ALTER TABLE tbl_shop_orders ADD COLUMN IF NOT EXISTS delivery_city VARCHAR(100);
ALTER TABLE tbl_shop_orders ADD COLUMN IF NOT EXISTS delivery_state VARCHAR(100);
ALTER TABLE tbl_shop_orders ADD COLUMN IF NOT EXISTS delivery_pincode VARCHAR(15);

-- Align tbl_shop_order_items schema with line_total
ALTER TABLE tbl_shop_order_items ADD COLUMN IF NOT EXISTS line_total NUMERIC(10,2) DEFAULT 0.00 CHECK (line_total >= 0.00);

-- Create payments table if not exists
CREATE TABLE IF NOT EXISTS tbl_shop_payments (
    id UUID DEFAULT random_uuid() PRIMARY KEY,
    order_id UUID NOT NULL UNIQUE REFERENCES tbl_shop_orders(id) ON DELETE CASCADE,
    payment_provider VARCHAR(50) NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    transaction_id VARCHAR(100) UNIQUE NOT NULL,
    amount NUMERIC(10,2) NOT NULL CHECK (amount >= 0.00),
    payment_status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Performance indices
CREATE INDEX IF NOT EXISTS idx_shop_orders_customer_status ON tbl_shop_orders(customer_id, status);
CREATE INDEX IF NOT EXISTS idx_shop_order_items_order_id ON tbl_shop_order_items(order_id);
CREATE INDEX IF NOT EXISTS idx_shop_payments_order_id ON tbl_shop_payments(order_id);
