#!/usr/bin/env bash
# =========================================================================
# STATE SMART LIFE - OPERATIONAL LOAD TEST EXECUTION SCRIPT
# Baseline Commit: a14e9a0
# =========================================================================

set -euo pipefail

TARGET_URL="${TARGET_URL:-http://localhost:8081/api/v1}"

echo "[$(date)] Launching k6 Operational Load Test against ${TARGET_URL}..."

if command -v k6 &> /dev/null; then
    k6 run --env TARGET_URL="${TARGET_URL}" scripts/k6-load-test.js
else
    echo "[INFO] k6 is not installed on host path. Executing load test via official k6 Docker container..."
    docker run --rm -i --network="host" -e TARGET_URL="${TARGET_URL}" grafana/k6 run - < scripts/k6-load-test.js
fi

echo "[$(date)] Load test execution completed!"
