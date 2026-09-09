-- V12__Infrastructure_Tourism_Transport_Enhancements.sql

-- 1. Tourism Places
CREATE TABLE IF NOT EXISTS tbl_tourism_places (
    id UUID PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    description_en TEXT NOT NULL,
    description_local TEXT,
    category VARCHAR(50) NOT NULL,
    latitude NUMERIC(10,7) NOT NULL,
    longitude NUMERIC(10,7) NOT NULL,
    average_visit_duration_minutes INT NOT NULL DEFAULT 60,
    entry_fee NUMERIC(8,2) NOT NULL DEFAULT 0.00 CHECK (entry_fee >= 0.00),
    image_url VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_tourism_category CHECK (category IN ('HERITAGE', 'NATURE', 'TEMPLE', 'BEACH'))
);

-- 2. Tourism Guide Applications
CREATE TABLE IF NOT EXISTS tbl_tourism_guide_applications (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES tbl_auth_users(id) ON DELETE CASCADE,
    full_name VARCHAR(100) NOT NULL,
    license_number VARCHAR(100) NOT NULL,
    id_proof_url VARCHAR(255),
    experience_years INT NOT NULL DEFAULT 0,
    languages_spoken VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    rejection_reason TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_app_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED'))
);

-- 3. Tourism Certified Guides
CREATE TABLE IF NOT EXISTS tbl_tourism_guides (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE REFERENCES tbl_auth_users(id) ON DELETE CASCADE,
    full_name VARCHAR(100) NOT NULL,
    professional_headline VARCHAR(150),
    about_me TEXT,
    profile_image_url VARCHAR(255),
    cover_image_url VARCHAR(255),
    languages_spoken VARCHAR(255) NOT NULL,
    experience_years INT NOT NULL DEFAULT 0,
    specialization VARCHAR(255),
    hourly_rate NUMERIC(8,2) NOT NULL DEFAULT 0.00 CHECK (hourly_rate >= 0.00),
    is_verified BOOLEAN NOT NULL DEFAULT TRUE,
    is_available BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 4. Guide Photo Gallery
CREATE TABLE IF NOT EXISTS tbl_tourism_guide_gallery (
    id UUID PRIMARY KEY,
    guide_id UUID NOT NULL REFERENCES tbl_tourism_guides(id) ON DELETE CASCADE,
    image_url VARCHAR(255) NOT NULL,
    caption VARCHAR(150),
    display_order INT NOT NULL DEFAULT 0,
    is_cover BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 5. Guide Covered Destinations Mapping
CREATE TABLE IF NOT EXISTS tbl_tourism_guide_destinations (
    id UUID PRIMARY KEY,
    guide_id UUID NOT NULL REFERENCES tbl_tourism_guides(id) ON DELETE CASCADE,
    tourism_place_id UUID NOT NULL REFERENCES tbl_tourism_places(id) ON DELETE CASCADE,
    CONSTRAINT uq_guide_place UNIQUE (guide_id, tourism_place_id)
);

-- 6. Guide Bookings
CREATE TABLE IF NOT EXISTS tbl_tourism_guide_bookings (
    id UUID PRIMARY KEY,
    guide_id UUID NOT NULL REFERENCES tbl_tourism_guides(id) ON DELETE CASCADE,
    tourist_id UUID NOT NULL REFERENCES tbl_auth_users(id) ON DELETE CASCADE,
    booking_date DATE NOT NULL,
    duration_hours INT NOT NULL CHECK (duration_hours > 0),
    total_fee NUMERIC(10,2) NOT NULL CHECK (total_fee >= 0.00),
    status VARCHAR(50) NOT NULL DEFAULT 'BOOKED',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_guide_booking_date UNIQUE (guide_id, booking_date)
);

-- 7. Transport Routes
CREATE TABLE IF NOT EXISTS tbl_transport_routes (
    id UUID PRIMARY KEY,
    route_number VARCHAR(20) NOT NULL,
    vehicle_type VARCHAR(50) NOT NULL,
    start_point VARCHAR(100) NOT NULL,
    end_point VARCHAR(100) NOT NULL,
    fare_price NUMERIC(8,2) NOT NULL DEFAULT 0.00 CHECK (fare_price >= 0.00),
    frequency_minutes INT NOT NULL DEFAULT 15,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_vehicle_type CHECK (vehicle_type IN ('MO_BUS', 'METRO', 'E_RICKSHAW', 'AUTO'))
);

-- 8. Event Venues
CREATE TABLE IF NOT EXISTS tbl_event_venues (
    id UUID PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    address TEXT NOT NULL,
    latitude NUMERIC(10,7) NOT NULL,
    longitude NUMERIC(10,7) NOT NULL,
    capacity INT NOT NULL CHECK (capacity > 0),
    price_per_day NUMERIC(10,2) NOT NULL CHECK (price_per_day >= 0.00),
    amenities TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 9. Event Bookings
CREATE TABLE IF NOT EXISTS tbl_event_bookings (
    id UUID PRIMARY KEY,
    venue_id UUID NOT NULL REFERENCES tbl_event_venues(id) ON DELETE RESTRICT,
    planner_id UUID NOT NULL REFERENCES tbl_auth_users(id) ON DELETE CASCADE,
    event_date DATE NOT NULL,
    total_budget NUMERIC(12,2) NOT NULL CHECK (total_budget >= 0.00),
    vendor_requirements TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'CONFIRMED',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_event_venue_date UNIQUE (venue_id, event_date)
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_tourism_places_cat ON tbl_tourism_places (category);
CREATE INDEX IF NOT EXISTS idx_transport_routes_num ON tbl_transport_routes (route_number);
CREATE INDEX IF NOT EXISTS idx_event_bookings_venue ON tbl_event_bookings (venue_id, event_date);
