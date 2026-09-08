# State Smart Life — Database Schema & Verification Guide

This directory houses the primary database design files for the PostgreSQL database backed by PostGIS spatial extensions.

## Structure
* [schema.sql](file:///d:/Dev/Projects/MO%20ODISHA/db/schema.sql): Complete DDL mapping all tables, relationships, and indices.

---

## 1. Local Setup Instructions

Ensure PostGIS is installed on your local PostgreSQL server, then run the schema initialization:

```bash
# Connect and run DDL commands
psql -U ssl_admin -d state_smart_life -f db/schema.sql
```

---

## 2. Geospatial Index Verification Queries

To verify that the PostGIS GIST indexes are functioning correctly and database engines are utilizing spatial indexes rather than full table sequential scans, execute the following commands in the PostgreSQL command line client.

### 2.1 Explain Plan Check for Nearby Store Lookup
Run `EXPLAIN ANALYZE` on a spatial query to check if `idx_shop_stores_loc` is active:

```sql
EXPLAIN ANALYZE
SELECT id, name 
FROM tbl_shop_stores 
WHERE ST_DWithin(location, ST_MakePoint(85.8245, 20.2961)::geography, 5000);
```

**Expected Output Indicators:**
- Look for `Bitmap Index Scan on idx_shop_stores_loc` or `Index Scan using idx_shop_stores_loc`.
- Ensure it does not indicate `Seq Scan on tbl_shop_stores` for large datasets.

### 2.2 Verify Geodetic Distance Calculations
Calculate distance in meters to verify coordination accuracy:

```sql
SELECT name, 
       ST_Distance(location, ST_MakePoint(85.8245, 20.2961)::geography) AS distance_meters
FROM tbl_shop_stores
ORDER BY distance_meters LIMIT 5;
```

---

## 3. Database Migration Strategy
For production tracking, Flyway or Liquibase migrations should be configured in the Spring Boot application pointing to `/src/main/resources/db/migration/`.
To align with Flyway conventions, name this migration file `V1__Initial_Schema.sql`.
