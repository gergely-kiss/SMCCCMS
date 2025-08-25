#!/usr/bin/env bash
set -euo pipefail

# Optional local overrides
if [ -f ops/env ]; then
  # shellcheck disable=SC1091
  source ops/env
fi

echo "▶ Starting Postgres on host port 57542…"
docker compose -f ops/docker-compose.db.yml up -d

echo "▶ Waiting for DB to be ready…"
until docker exec smcccms_db pg_isready -U pg_admin -d smcccms >/dev/null 2>&1; do
  sleep 1
done
echo "✓ DB ready"

# Backend
if [ -d smcccms_be ]; then
  echo "▶ Starting backend (Spring Boot)…"
  (cd smcccms_be && ./mvnw -q spring-boot:run || mvn -q spring-boot:run)
else
  echo "ℹ️ Backend folder smcccms_be not found yet. Add it or run FE separately."
fi

# Frontend note (to run in another terminal once FE exists):
#   cd smcccms_fe && npm ci && npm run dev
