-- =========================================================================
-- STATE SMART LIFE - COMPLETE POSTGRESQL DATABASE SCHEMA
-- =========================================================================

-- Enable PostGIS extension for geospatial functionality
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS postgis;

-- Clean existing tables if needed (careful in production)
-- DROP TABLE IF EXISTS tbl_trust_scores CASCADE;
-- DROP TABLE IF EXISTS tbl_event_bookings CASCADE;
-- DROP TABLE IF EXISTS tbl_event_venues CASCADE;
-- DROP TABLE IF EXISTS tbl_transport_routes CASCADE;
-- DROP TABLE IF EXISTS tbl_tourism_guides CASCADE;
-- DROP TABLE IF EXISTS tbl_tourism_places CASCADE;
-- DROP TABLE IF EXISTS tbl_emergency_requests CASCADE;
-- DROP TABLE IF EXISTS tbl_hc_appointments CASCADE;
-- DROP TABLE IF EXISTS tbl_hc_doctors CASCADE;
-- DROP TABLE IF EXISTS tbl_hc_hospitals CASCADE;
-- DROP TABLE IF EXISTS tbl_deliveries CASCADE;
-- DROP TABLE IF EXISTS tbl_shop_orders CASCADE;
-- DROP TABLE IF EXISTS tbl_shop_inventory CASCADE;
-- DROP TABLE IF EXISTS tbl_shop_products CASCADE;
-- DROP TABLE IF EXISTS tbl_shop_stores CASCADE;
-- DROP TABLE IF EXISTS tbl_user_profiles CASCADE;
-- DROP TABLE IF EXISTS tbl_auth_users CASCADE;
-- DROP TYPE IF EXISTS user_role CASCADE;

-- Create User Role enum type
CREATE TYPE user_role AS ENUM ('CUSTOMER', 'SHOP_OWNER', 'DELIVERY_PARTNER', 'HEALTHCARE_PROVIDER', 'ADMIN');

-- 1. AUTH MODULE
CREATE TABLE tbl_auth_users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    phone_number VARCHAR(15) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role user_role NOT NULL,
    is_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 2. USER MANAGEMENT MODULE
CREATE TABLE tbl_user_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE REFERENCES tbl_auth_users(id) ON DELETE CASCADE,
    full_name VARCHAR(100) NOT NULL,
    avatar_url VARCHAR(255),
    emergency_contact VARCHAR(15),
    home_address TEXT,
    last_location GEOGRAPHY(Point, 4326),
    last_active_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 3. TRUST & VERIFICATION SYSTEM (LOCAL TRUST GRAPH)
CREATE TABLE tbl_trust_scores (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entity_id UUID UNIQUE NOT NULL, -- Points to User, Shop, Doctor etc.
    entity_type VARCHAR(50) NOT NULL, -- 'SHOP', 'DELIVERY_PARTNER', 'HEALTHCARE_PROVIDER'
    verified_transactions_count INT DEFAULT 0,
    repeat_users_count INT DEFAULT 0,
    complaints_count INT DEFAULT 0,
    trust_score NUMERIC(5,2) DEFAULT 100.00 CHECK (trust_score >= 0.00 AND trust_score <= 100.00),
    badge_level VARCHAR(20) DEFAULT 'NONE', -- 'NONE', 'BRONZE', 'SILVER', 'GOLD', 'PLATINUM'
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 4. SHOPPING & HYPERLOCAL INVENTORY MODULE
CREATE TABLE tbl_shop_stores (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id UUID NOT NULL REFERENCES tbl_auth_users(id) ON DELETE RESTRICT,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    category VARCHAR(50) NOT NULL, -- 'GROCERY', 'PHARMACY', 'RESTOCK', 'ELECTRONICS'
    address TEXT NOT NULL,
    location GEOGRAPHY(Point, 4326) NOT NULL,
    opening_time TIME NOT NULL,
    closing_time TIME NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE tbl_shop_products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
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

CREATE TABLE tbl_shop_inventory (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID UNIQUE NOT NULL REFERENCES tbl_shop_products(id) ON DELETE CASCADE,
    stock_quantity INT NOT NULL DEFAULT 0 CHECK (stock_quantity >= 0),
    low_stock_threshold INT DEFAULT 5,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 5. ORDERS & DELIVERY MODULES
CREATE TABLE tbl_shop_orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL REFERENCES tbl_auth_users(id) ON DELETE RESTRICT,
    store_id UUID NOT NULL REFERENCES tbl_shop_stores(id) ON DELETE RESTRICT,
    total_amount NUMERIC(10,2) NOT NULL CHECK (total_amount >= 0.00),
    status VARCHAR(50) NOT NULL, -- 'PENDING', 'ACCEPTED', 'PREPARING', 'READY_FOR_PICKUP', 'DELIVERED', 'CANCELLED'
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE tbl_deliveries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL UNIQUE REFERENCES tbl_shop_orders(id) ON DELETE CASCADE,
    delivery_partner_id UUID REFERENCES tbl_auth_users(id) ON DELETE SET NULL,
    pickup_address TEXT NOT NULL,
    dropoff_address TEXT NOT NULL,
    pickup_location GEOGRAPHY(Point, 4326) NOT NULL,
    dropoff_location GEOGRAPHY(Point, 4326) NOT NULL,
    status VARCHAR(50) NOT NULL, -- 'ASSIGNED', 'PICKED_UP', 'IN_TRANSIT', 'DELIVERED', 'FAILED'
    delivery_fee NUMERIC(8,2) NOT NULL DEFAULT 0.00 CHECK (delivery_fee >= 0.00),
    estimated_arrival TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 6. HEALTHCARE MODULE
CREATE TABLE tbl_hc_hospitals (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(150) NOT NULL,
    address TEXT NOT NULL,
    location GEOGRAPHY(Point, 4326) NOT NULL,
    phone_number VARCHAR(15) NOT NULL,
    has_emergency_service BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE tbl_hc_doctors (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    hospital_id UUID REFERENCES tbl_hc_hospitals(id) ON DELETE SET NULL,
    name VARCHAR(100) NOT NULL,
    specialization VARCHAR(100) NOT NULL,
    availability_schedule JSONB, -- Day mapping, time slots, e.g. [{"day": "Monday", "slots": ["09:00-12:00"]}]
    license_number VARCHAR(100) UNIQUE NOT NULL,
    consultation_fee NUMERIC(8,2) NOT NULL DEFAULT 0.00 CHECK (consultation_fee >= 0.00),
    is_available BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE tbl_hc_appointments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    doctor_id UUID NOT NULL REFERENCES tbl_hc_doctors(id) ON DELETE RESTRICT,
    patient_id UUID NOT NULL REFERENCES tbl_auth_users(id) ON DELETE CASCADE,
    appointment_time TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'BOOKED', -- 'BOOKED', 'COMPLETED', 'CANCELLED'
    symptoms_description TEXT,
    prescription_url VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 7. EMERGENCY RESPONSE MODULE
CREATE TABLE tbl_emergency_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    requester_id UUID REFERENCES tbl_auth_users(id) ON DELETE SET NULL,
    requester_phone VARCHAR(15) NOT NULL,
    emergency_type VARCHAR(50) NOT NULL, -- 'MEDICAL', 'FIRE', 'POLICE', 'DISASTER'
    location GEOGRAPHY(Point, 4326) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'REPORTED', -- 'REPORTED', 'DISPATCHED', 'RESOLVED'
    assigned_ambulance_id UUID,
    reported_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 8. TOURISM MODULE
CREATE TABLE tbl_tourism_places (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(150) NOT NULL,
    description_en TEXT NOT NULL,
    description_local TEXT, -- Local language details
    category VARCHAR(50), -- 'TEMPLE', 'NATURE', 'HISTORIC', 'BEACH'
    location GEOGRAPHY(Point, 4326) NOT NULL,
    average_visit_duration INT, -- in minutes
    entry_fee NUMERIC(8,2) DEFAULT 0.00 CHECK (entry_fee >= 0.00),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE tbl_tourism_guides (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE REFERENCES tbl_auth_users(id) ON DELETE CASCADE,
    languages_spoken TEXT[] NOT NULL,
    hourly_rate NUMERIC(8,2) NOT NULL CHECK (hourly_rate >= 0.00),
    operating_area GEOGRAPHY(Polygon, 4326),
    is_available BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 9. TRANSPORT MODULE
CREATE TABLE tbl_transport_routes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    route_number VARCHAR(20) NOT NULL,
    vehicle_type VARCHAR(50) NOT NULL, -- 'BUS', 'AUTO_RICKSHAW', 'FERRY'
    start_point VARCHAR(100) NOT NULL,
    end_point VARCHAR(100) NOT NULL,
    path_geometry GEOGRAPHY(LineString, 4326) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 10. EVENT PLANNING MODULE
CREATE TABLE tbl_event_venues (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(150) NOT NULL,
    address TEXT NOT NULL,
    location GEOGRAPHY(Point, 4326) NOT NULL,
    capacity INT NOT NULL CHECK (capacity > 0),
    price_per_day NUMERIC(10,2) NOT NULL CHECK (price_per_day >= 0.00),
    amenities JSONB, -- list of features like catering, AC, parking
    is_booked_out BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE tbl_event_bookings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    venue_id UUID NOT NULL REFERENCES tbl_event_venues(id) ON DELETE RESTRICT,
    planner_id UUID NOT NULL REFERENCES tbl_auth_users(id) ON DELETE CASCADE,
    event_date DATE NOT NULL,
    total_budget NUMERIC(12,2) CHECK (total_budget >= 0.00),
    vendor_requirements JSONB,
    status VARCHAR(50) NOT NULL DEFAULT 'CONFIRMED',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- =========================================================================
-- INDEXES FOR GEOSPATIAL SEARCH OPTIMIZATION
-- =========================================================================

-- GIST Indexes for coordinates querying
CREATE INDEX idx_user_profiles_loc ON tbl_user_profiles USING GIST(last_location);
CREATE INDEX idx_shop_stores_loc ON tbl_shop_stores USING GIST(location);
CREATE INDEX idx_hc_hospitals_loc ON tbl_hc_hospitals USING GIST(location);
CREATE INDEX idx_emergency_requests_loc ON tbl_emergency_requests USING GIST(location);
CREATE INDEX idx_tourism_places_loc ON tbl_tourism_places USING GIST(location);
CREATE INDEX idx_transport_routes_path ON tbl_transport_routes USING GIST(path_geometry);
CREATE INDEX idx_event_venues_loc ON tbl_event_venues USING GIST(location);

-- B-Tree search optimization indexes
CREATE INDEX idx_auth_users_phone ON tbl_auth_users(phone_number);
CREATE INDEX idx_user_profiles_user ON tbl_user_profiles(user_id);
CREATE INDEX idx_shop_products_store ON tbl_shop_products(store_id);
CREATE INDEX idx_shop_inventory_product ON tbl_shop_inventory(product_id);
CREATE INDEX idx_shop_orders_customer ON tbl_shop_orders(customer_id);
CREATE INDEX idx_deliveries_order ON tbl_deliveries(order_id);
CREATE INDEX idx_deliveries_partner ON tbl_deliveries(delivery_partner_id);
CREATE INDEX idx_hc_doctors_hospital ON tbl_hc_doctors(hospital_id);
CREATE INDEX idx_hc_appointments_doctor ON tbl_hc_appointments(doctor_id);
CREATE INDEX idx_hc_appointments_patient ON tbl_hc_appointments(patient_id);
CREATE INDEX idx_trust_entity ON tbl_trust_scores(entity_id, entity_type);
CREATE INDEX idx_event_bookings_venue ON tbl_event_bookings(venue_id);
CREATE INDEX idx_event_bookings_planner ON tbl_event_bookings(planner_id);
