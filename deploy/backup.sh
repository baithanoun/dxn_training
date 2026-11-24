#!/usr/bin/env bash
# Back up the production MySQL database to a compressed dump.
# Usage: DB_HOST=... DB_PORT=3306 DB_DATABASE=... DB_USERNAME=... DB_PASSWORD=... ./deploy/backup.sh [output-dir]
# Passwords are read from the environment; never hard-code them.

set -euo pipefail

: "${DB_HOST:?DB_HOST is required}"
: "${DB_DATABASE:?DB_DATABASE is required}"
: "${DB_USERNAME:?DB_USERNAME is required}"
: "${DB_PASSWORD:?DB_PASSWORD is required}"

DB_PORT="${DB_PORT:-3306}"
OUT_DIR="${1:-.}"
mkdir -p "$OUT_DIR"

STAMP="$(date -u +%Y%m%dT%H%M%SZ)"
OUT_FILE="$OUT_DIR/dxn_${DB_DATABASE}_${STAMP}.sql.gz"

mysqldump \
  --host="$DB_HOST" \
  --port="$DB_PORT" \
  --user="$DB_USERNAME" \
  --password="$DB_PASSWORD" \
  --single-transaction \
  --routines \
  --triggers \
  "$DB_DATABASE" | gzip > "$OUT_FILE"

echo "Backup written to $OUT_FILE"
```