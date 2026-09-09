#!/usr/bin/env bash
# =========================================================================
# STATE SMART LIFE - POSTGRESQL RESTORE VERIFICATION SCRIPT
# Target RTO Benchmark: < 15 Minutes
# =========================================================================

set -euo pipefail

if [ -z "${1:-}" ]; then
  echo "Usage: $0 <path-to-backup-file.sql.gz>"
  exit 1
fi

BACKUP_FILE="$1"
CONTAINER_NAME="${CONTAINER_NAME:-ssl-postgres}"
DB_USER="${POSTGRES_USER:-ssl_admin}"
DB_NAME="${POSTGRES_DB:-state_smart_life}"

echo "[$(date)] Initiating database restoration from ${BACKUP_FILE}..."

gunzip -c "${BACKUP_FILE}" | docker exec -i "${CONTAINER_NAME}" psql -U "${DB_USER}" -d "${DB_NAME}"

echo "[$(date)] Database restoration completed successfully!"
