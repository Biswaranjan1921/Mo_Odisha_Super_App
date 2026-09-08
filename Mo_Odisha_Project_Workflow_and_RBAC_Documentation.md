# STATE SMART LIFE (MO ODISHA SUPER APP)
## Full Technical Architecture, Role-Based Access Control (RBAC) & Workflow Specification

---

## Executive Summary

**State Smart Life (Mo Odisha)** is an integrated, modular super-app platform designed for digital governance and hyper-local service delivery across all 30 districts of Odisha. Built with a high-performance **Spring Boot 3.1.5** backend, **PostgreSQL 15 + PostGIS** database engine, **Redis** cache layer, and a **React 18 + Vite** frontend with bilingual voice guidance, the application empowers citizens, local merchants, delivery fleets, medical providers, and government officers.

---

## 1. System Architecture Diagram

```
[ Frontend: React 18 + Vite ]
   ├── Bilingual Voice Engine (English / Odia)
   ├── Accessibility / High-Contrast Elderly Mode
   └── Leaflet GIS Live Maps & Route Tracking
               │
       (HTTP / REST API Proxy)
               │
[ Backend: Spring Boot 3.1.5 ] (Port 8081)
   ├── Spring Security JWT Auth
   ├── Citizen KYC & Aadhaar Verification Module
   ├── Hyperlocal Store & Inventory Engine
   ├── Logistics & Parcel Handover Controller
   ├── Healthcare Telehealth Planner
   ├── Emergency SOS Command Dispatcher
   └── Trust Graph & Ratings Engine
               │
    ┌──────────┴──────────┐
    ▼                     ▼
[ PostgreSQL 15 ]    [ Redis 7.0 ]
 + PostGIS Spatial    In-Memory Cache
```

---

## 2. Role-Based Access Control (RBAC) Matrix

The system enforces strict RBAC governed by the `user_role` enumeration (`CUSTOMER`, `SHOP_OWNER`, `DELIVERY_PARTNER`, `HEALTHCARE_PROVIDER`, `ADMIN`).

| Capability / Module | `CUSTOMER` (Buyer) | `SHOP_OWNER` (Seller) | `DELIVERY_PARTNER` (Logistics) | `HEALTHCARE_PROVIDER` (Doctor) | `ADMIN` (Approver / Officer) |
| :--- | :---: | :---: | :---: | :---: | :---: |
| **Citizen KYC Authentication** | ✅ | ✅ | ✅ | ✅ | ✅ |
| **District Selector & Navigation** | ✅ | ✅ | ✅ | ✅ | ✅ |
| **Browse Stores & Products Catalog** | ✅ | ✅ | ✅ | ✅ | ✅ |
| **Place Hyperlocal Orders** | ✅ | ❌ | ❌ | ❌ | ❌ |
| **Store & Inventory Management** | ❌ | ✅ | ❌ | ❌ | ✅ |
| **View & Claim Delivery Tasks** | ❌ | ❌ | ✅ | ❌ | ✅ |
| **Update Delivery Telemetry (`PICKED_UP`, `DELIVERED`)** | ❌ | ❌ | ✅ | ❌ | ❌ |
| **Book Doctor Consultations** | ✅ | ❌ | ❌ | ❌ | ❌ |
| **Manage Clinic Schedule & Fees** | ❌ | ❌ | ❌ | ✅ | ✅ |
| **Trigger SOS Emergency Broadcast** | ✅ | ✅ | ✅ | ✅ | ✅ |
| **Dispatch Emergency Response Vehicles** | ❌ | ❌ | ❌ | ❌ | ✅ |
| **Book Tour Guides & Event Venues** | ✅ | ❌ | ❌ | ❌ | ❌ |
| **File Citizen Complaints / Incident Tickets** | ✅ | ✅ | ✅ | ✅ | ✅ |
| **Approve Merchant Badges & Resolve Disputes** | ❌ | ❌ | ❌ | ❌ | ✅ |

---

## 3. End-to-End Workflow Specifications

### Workflow 3.1: Multi-Stage Citizen Verification & Authentication (KYC)

1. **Step 1 — Initial Credentials & Math CAPTCHA:**
   - Citizen inputs Phone Number (+91), Email Address, Age, and solves a dynamic math CAPTCHA (e.g. `5 + 3 = 8`).
2. **Step 2 — Aadhaar Identity Verification:**
   - Citizen inputs 12-digit Aadhaar Card Number. System issues a 6-digit OTP to registered mobile (Testing code: `448822`).
3. **Step 3 — Dual-Factor Email Security:**
   - System dispatches a 6-digit email confirmation code (Testing code: `998877`).
4. **Step 4 — PIN Lock & Elderly Mode Adapter:**
   - Citizen creates 4-digit security PIN.
   - **Automated Accessibility Mode:** If citizen age >= 60, High-Contrast Elderly Mode activates automatically.

---

### Workflow 3.2: Hyperlocal Commerce & Logistics (Buyer <-> Seller <-> Delivery Workflow)

```
[CUSTOMER / BUYER]               [SHOP OWNER / SELLER]           [DELIVERY PARTNER]
       │                                   │                              │
 1. Select Store & Add Items               │                              │
 2. Place Order ─────────────────────────> │                              │
       │                            3. Order Status set to                │
       │                               'PENDING' / 'READY'                │
       │                                   │                              │
       │                                   ├────────────────────────────> │
       │                                   │                        4. Claim Package
       │                                   │                           ('PICKED_UP')
       │                                   │                              │
       │ <─────────────────────────────────┼──────────────────────────────┤
 5. Monitor Live Delivery Progress         │                        6. Handover & Mark
    on Parcel Worksheet                    │                           'DELIVERED'
```

1. **Buyer Action:**
   - Navigates to `/shopping`, selects a local merchant (e.g. *Kalinga Pharmacy*, *Bhubaneswar Daily Groceries*).
   - Adds items to cart and clicks **"Place Order & Request Delivery"**.
2. **System Action:**
   - Generates an `Order ID` (e.g. `ord-8421`) with status `PENDING`.
   - Creates a matching delivery worksheet saved to `tbl_deliveries`.
3. **Delivery Partner Action:**
   - Navigates to `/delivery` to view active parcel assignments.
   - Clicks **"Pick Up Parcel"** (status updates to `IN_TRANSIT` / `PICKED_UP`).
   - Voice assistant confirms: *"Package picked up. Delivery is now in transit."*
   - Completes drop-off and clicks **"Mark Delivered"** (status updates to `DELIVERED`).

---

### Workflow 3.3: Emergency SOS & GIS Command Dispatcher

1. **Panic Activation:**
   - Citizen clicks the central **SOS Emergency Button** or dials **108**.
2. **PostGIS GPS Target Lock:**
   - System captures exact spatial coordinates (`20.2961° N, 85.8245° E`) and displays position marker on interactive OpenStreetMap canvas.
3. **Automated Command Center Telemetry Stream:**
   - **T+0s:** GPS coordinates dispatched to State Emergency Registry.
   - **T+5s:** Command Officer matched and assigned to incident.
   - **T+12s:** Ambulance dispatched from Capital Hospital (ETA 5 minutes).

---

### Workflow 3.4: Citizen Trust Graph & Approver Verification Engine

1. **Dynamic Trust Score Formula:**
   Trust Score = 100.00 - (Complaints Count * 1.50) + (Verified Orders * 0.25)
2. **Badge Tier Progression:**
   - **Platinum:** 98.00% – 100.00%
   - **Gold:** 90.00% – 97.99%
   - **Silver:** 80.00% – 89.99%
   - **Bronze:** 70.00% – 79.99%
3. **Dispute Resolution:**
   - Citizens file feedback regarding overcharging or delay.
   - Ticket enters `UNDER_REVIEW` state for Government Approver audit.

---

## 4. Complete Database Schema Architecture (`db/schema.sql`)

```sql
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

-- 3. TRUST & VERIFICATION SYSTEM
CREATE TABLE tbl_trust_scores (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entity_id UUID UNIQUE NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    verified_transactions_count INT DEFAULT 0,
    repeat_users_count INT DEFAULT 0,
    complaints_count INT DEFAULT 0,
    trust_score NUMERIC(5,2) DEFAULT 100.00 CHECK (trust_score >= 0.00 AND trust_score <= 100.00),
    badge_level VARCHAR(20) DEFAULT 'NONE',
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 4. HYPERLOCAL STORES & INVENTORY MODULE
CREATE TABLE tbl_shop_stores (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id UUID NOT NULL REFERENCES tbl_auth_users(id) ON DELETE RESTRICT,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    category VARCHAR(50) NOT NULL,
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

-- 5. ORDERS & LOGISTICS MODULES
CREATE TABLE tbl_shop_orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL REFERENCES tbl_auth_users(id) ON DELETE RESTRICT,
    store_id UUID NOT NULL REFERENCES tbl_shop_stores(id) ON DELETE RESTRICT,
    total_amount NUMERIC(10,2) NOT NULL CHECK (total_amount >= 0.00),
    status VARCHAR(50) NOT NULL,
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
    status VARCHAR(50) NOT NULL,
    delivery_fee NUMERIC(8,2) NOT NULL DEFAULT 0.00 CHECK (delivery_fee >= 0.00),
    estimated_arrival TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- SPATIAL GIST INDEXES
CREATE INDEX idx_user_profiles_loc ON tbl_user_profiles USING GIST(last_location);
CREATE INDEX idx_shop_stores_loc ON tbl_shop_stores USING GIST(location);
CREATE INDEX idx_hc_hospitals_loc ON tbl_hc_hospitals USING GIST(location);
CREATE INDEX idx_emergency_requests_loc ON tbl_emergency_requests USING GIST(location);
CREATE INDEX idx_tourism_places_loc ON tbl_tourism_places USING GIST(location);
CREATE INDEX idx_transport_routes_path ON tbl_transport_routes USING GIST(path_geometry);
CREATE INDEX idx_event_venues_loc ON tbl_event_venues USING GIST(location);
```

---
*State Smart Life (Mo Odisha) Downloadable Specification Document.*
