// =========================================================================
// STATE SMART LIFE - K6 OPERATIONAL LOAD TESTING SUITE
// Baseline Commit: a14e9a0
// Target SLA: p95 < 200ms, Operational Error Rate < 0.1%
// =========================================================================

import http from 'k6/http';
import { check, group, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

// Custom Operational Metrics
export const operationalErrorRate = new Rate('operational_error_rate');
export const apiLatencyTrend = new Trend('api_latency_trend');

// Load Test Stage Profile (Virtual Users Ramp-Up)
export const options = {
    stages: [
        { duration: '30s', target: 20 },  // Ramp-up to 20 VUs
        { duration: '1m',  target: 50 },  // Sustain 50 VUs
        { duration: '30s', target: 100 }, // Peak load 100 VUs
        { duration: '30s', target: 0 },   // Cool-down
    ],
    thresholds: {
        http_req_duration: ['p(95)<200', 'p(99)<400'], // Latency SLA
        operational_error_rate: ['rate<0.001'],       // Error SLA (<0.1%)
    },
};

const BASE_URL = __ENV.TARGET_URL || 'http://localhost:8081/api/v1';

// Setup Phase: Authenticate and obtain JWT Bearer Token before running load test scenarios
export function setup() {
    const headers = { 'Content-Type': 'application/json' };
    const credentials = {
        email: 'staging_admin@smartlife.odisha.gov.in',
        password: 'AdminPassword123!'
    };

    // Attempt login first
    let res = http.post(`${BASE_URL}/auth/login`, JSON.stringify(credentials), { headers });

    if (res.status !== 200) {
        // Register staging admin user if missing
        const registerPayload = JSON.stringify({
            fullName: 'Staging Admin User',
            phoneNumber: '9988776655',
            email: credentials.email,
            password: credentials.password,
            dateOfBirth: '1990-01-01',
            role: 'ADMIN'
        });
        http.post(`${BASE_URL}/auth/register`, registerPayload, { headers });
        res = http.post(`${BASE_URL}/auth/login`, JSON.stringify(credentials), { headers });
    }

    if (res.status === 200) {
        const body = JSON.parse(res.body);
        return { token: body.accessToken || body.token };
    }
    return { token: null };
}

export default function (data) {
    const publicHeaders = { 'Content-Type': 'application/json' };
    const authHeaders = {
        'Content-Type': 'application/json',
        'Authorization': data && data.token ? `Bearer ${data.token}` : ''
    };

    // Scenario 1: System Health & Actuator Probes
    group('01_HealthCheck', function () {
        const res = http.get(`${BASE_URL}/actuator/health/readiness`, { headers: publicHeaders });
        const success = check(res, { 'Readiness 200 OK': (r) => r.status === 200 });
        operationalErrorRate.add(!success || res.status >= 500);
        apiLatencyTrend.add(res.timings.duration);
    });

    // Scenario 2: Public Service Discovery
    group('02_CitizenServiceDiscovery', function () {
        const res = http.get(`${BASE_URL}/system/info`, { headers: publicHeaders });
        const success = check(res, { 'System Info 200 OK': (r) => r.status === 200 });
        operationalErrorRate.add(!success || res.status >= 500);
        apiLatencyTrend.add(res.timings.duration);
    });

    // Scenario 3: Healthcare Hospital Catalogue (Authenticated Path)
    group('03_HealthcareHospitalLookup', function () {
        const res = http.get(`${BASE_URL}/healthcare/hospitals`, { headers: authHeaders });
        const success = check(res, { 'Healthcare 200 OK or 2xx': (r) => r.status >= 200 && r.status < 300 });
        operationalErrorRate.add(!success && res.status >= 500);
        apiLatencyTrend.add(res.timings.duration);
    });

    // Scenario 4: Governance Analytics Matrix (Authenticated Admin Path)
    group('04_GovernanceOverview', function () {
        const res = http.get(`${BASE_URL}/governance/analytics/overview`, { headers: authHeaders });
        const success = check(res, { 'Governance 200 OK or 2xx': (r) => r.status >= 200 && r.status < 300 });
        operationalErrorRate.add(!success && res.status >= 500);
        apiLatencyTrend.add(res.timings.duration);
    });

    sleep(1);
}
