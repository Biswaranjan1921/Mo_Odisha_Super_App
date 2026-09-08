-- =========================================================================
-- FLYWAY MIGRATION V1: INITIAL SCHEMA & TABLE DEFINITIONS
-- =========================================================================

-- 1. AUTH & VERIFICATION MODULE
CREATE TABLE IF NOT EXISTS tbl_auth_users (
    id UUID DEFAULT random_uuid() PRIMARY KEY,
    phone_number VARCHAR(15) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL,
    is_active BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tbl_email_verifications (
    id UUID DEFAULT random_uuid() PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES tbl_auth_users(id) ON DELETE CASCADE,
    verification_token VARCHAR(255) UNIQUE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tbl_refresh_tokens (
    id UUID DEFAULT random_uuid() PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES tbl_auth_users(id) ON DELETE CASCADE,
    token_hash VARCHAR(255) UNIQUE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 2. USER PROFILE & ACCESSIBILITY MODULE (DOB Age Engine)
CREATE TABLE IF NOT EXISTS tbl_user_profiles (
    id UUID DEFAULT random_uuid() PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE REFERENCES tbl_auth_users(id) ON DELETE CASCADE,
    full_name VARCHAR(100) NOT NULL,
    date_of_birth DATE NOT NULL,
    avatar_url VARCHAR(255),
    emergency_contact VARCHAR(15),
    home_address TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 3. COMMERCE & HYPERLOCAL INVENTORY MODULE
CREATE TABLE IF NOT EXISTS tbl_shop_stores (
    id UUID DEFAULT random_uuid() PRIMARY KEY,
    owner_id UUID NOT NULL REFERENCES tbl_auth_users(id) ON DELETE RESTRICT,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    category VARCHAR(50) NOT NULL,
    address TEXT NOT NULL,
    opening_time TIME NOT NULL,
    closing_time TIME NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tbl_shop_products (
    id UUID DEFAULT random_uuid() PRIMARY KEY,
    store_id UUID NOT NULL REFERENCES tbl_shop_stores(id) ON DELETE CASCADE,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    price NUMERIC(10,2) NOT NULL CHECK (price >= 0.00),
    sku VARCHAR(100),
    image_url VARCHAR(255),
    is_medicine BOOLEAN DEFAULT FALSE,
    requires_prescription BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tbl_shop_inventory (
    id UUID DEFAULT random_uuid() PRIMARY KEY,
    product_id UUID UNIQUE NOT NULL REFERENCES tbl_shop_products(id) ON DELETE CASCADE,
    stock_quantity INT NOT NULL DEFAULT 0 CHECK (stock_quantity >= 0),
    low_stock_threshold INT DEFAULT 5,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 4. ORDERS & ORDER ITEMS MODULE (One-to-Many Order Items)
CREATE TABLE IF NOT EXISTS tbl_shop_orders (
    id UUID DEFAULT random_uuid() PRIMARY KEY,
    customer_id UUID NOT NULL REFERENCES tbl_auth_users(id) ON DELETE RESTRICT,
    store_id UUID NOT NULL REFERENCES tbl_shop_stores(id) ON DELETE RESTRICT,
    total_amount NUMERIC(10,2) NOT NULL CHECK (total_amount >= 0.00),
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tbl_shop_order_items (
    id UUID DEFAULT random_uuid() PRIMARY KEY,
    order_id UUID NOT NULL REFERENCES tbl_shop_orders(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES tbl_shop_products(id) ON DELETE RESTRICT,
    quantity INT NOT NULL CHECK (quantity > 0),
    unit_price_at_purchase NUMERIC(10,2) NOT NULL CHECK (unit_price_at_purchase >= 0.00),
    subtotal NUMERIC(10,2) NOT NULL CHECK (subtotal >= 0.00),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 5. LOGISTICS & DELIVERIES MODULE
CREATE TABLE IF NOT EXISTS tbl_deliveries (
    id UUID DEFAULT random_uuid() PRIMARY KEY,
    order_id UUID NOT NULL UNIQUE REFERENCES tbl_shop_orders(id) ON DELETE CASCADE,
    delivery_partner_id UUID REFERENCES tbl_auth_users(id) ON DELETE SET NULL,
    pickup_address TEXT NOT NULL,
    dropoff_address TEXT NOT NULL,
    status VARCHAR(50) NOT NULL,
    delivery_fee NUMERIC(8,2) NOT NULL DEFAULT 0.00 CHECK (delivery_fee >= 0.00),
    estimated_arrival TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 6. HEALTHCARE MODULE
CREATE TABLE IF NOT EXISTS tbl_hc_hospitals (
    id UUID DEFAULT random_uuid() PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    address TEXT NOT NULL,
    phone_number VARCHAR(15) NOT NULL,
    has_emergency_service BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tbl_hc_doctors (
    id UUID DEFAULT random_uuid() PRIMARY KEY,
    hospital_id UUID REFERENCES tbl_hc_hospitals(id) ON DELETE SET NULL,
    name VARCHAR(100) NOT NULL,
    specialization VARCHAR(100) NOT NULL,
    availability_schedule VARCHAR(500),
    license_number VARCHAR(100) UNIQUE NOT NULL,
    consultation_fee NUMERIC(8,2) NOT NULL DEFAULT 0.00 CHECK (consultation_fee >= 0.00),
    is_available BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tbl_hc_appointments (
    id UUID DEFAULT random_uuid() PRIMARY KEY,
    doctor_id UUID NOT NULL REFERENCES tbl_hc_doctors(id) ON DELETE RESTRICT,
    patient_id UUID NOT NULL REFERENCES tbl_auth_users(id) ON DELETE CASCADE,
    appointment_time TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'BOOKED',
    symptoms_description TEXT,
    prescription_url VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 7. EMERGENCY SOS MODULE
CREATE TABLE IF NOT EXISTS tbl_emergency_requests (
    id UUID DEFAULT random_uuid() PRIMARY KEY,
    requester_id UUID REFERENCES tbl_auth_users(id) ON DELETE SET NULL,
    requester_phone VARCHAR(15) NOT NULL,
    emergency_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'REPORTED',
    assigned_ambulance_id UUID,
    reported_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 8. TRUST GRAPH MODULE (Bounded 0-100 Rating Engine)
CREATE TABLE IF NOT EXISTS tbl_trust_scores (
    id UUID DEFAULT random_uuid() PRIMARY KEY,
    entity_id UUID UNIQUE NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    verified_transactions_count INT DEFAULT 0,
    repeat_users_count INT DEFAULT 0,
    complaints_count INT DEFAULT 0,
    trust_score NUMERIC(5,2) DEFAULT 100.00 CHECK (trust_score >= 0.00 AND trust_score <= 100.00),
    badge_level VARCHAR(20) DEFAULT 'NONE',
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- B-Tree Performance Indexes
CREATE INDEX IF NOT EXISTS idx_auth_users_email ON tbl_auth_users(email);
CREATE INDEX IF NOT EXISTS idx_user_profiles_user ON tbl_user_profiles(user_id);
CREATE INDEX IF NOT EXISTS idx_shop_products_store ON tbl_shop_products(store_id);
CREATE INDEX IF NOT EXISTS idx_shop_orders_customer ON tbl_shop_orders(customer_id);
CREATE INDEX IF NOT EXISTS idx_order_items_order ON tbl_shop_order_items(order_id);
CREATE INDEX IF NOT EXISTS idx_deliveries_order ON tbl_deliveries(order_id);
CREATE INDEX IF NOT EXISTS idx_hc_appointments_doctor ON tbl_hc_appointments(doctor_id);
