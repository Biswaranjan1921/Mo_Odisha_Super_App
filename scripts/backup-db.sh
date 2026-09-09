#!/usr/bin/env bash
# =========================================================================
# STATE SMART LIFE - POSTGRESQL AUTOMATED BACKUP SCRIPT
# Target RPO Benchmark: < 5 Minutes
# =========================================================================

set -euo pipefail

BACKUP_DIR="${BACKUP_DIR:-./backups}"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
CONTAINER_NAME="${CONTAINER_NAME:-ssl-postgres}"
DB_USER="${POSTGRES_USER:-ssl_admin}"
DB_NAME="${POSTGRES_DB:-state_smart_life}"
BACKUP_FILE="${BACKUP_DIR}/${DB_NAME}_backup_${TIMESTAMP}.sql.gz"

mkdir -p "${BACKUP_DIR}"

echo "[$(date)] Initiating database backup for container ${CONTAINER_NAME}..."

docker exec -t "${CONTAINER_NAME}" pg_dump -U "${DB_USER}" "${DB_NAME}" | gzip > "${BACKUP_FILE}"

echo "[$(date)] Backup completed successfully: ${BACKUP_FILE}"
echo "[$(date)] Backup size: $(du -h "${BACKUP_FILE}" | cut -f1)"
