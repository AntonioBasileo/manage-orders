#!/bin/bash

# Interrompe lo script in caso di errore
set -e

cd /manage-orders

DB_HOST="${DB_HOST:-db-service.general.svc.cluster.local}"
DB_PORT="${DB_PORT:-3306}"
MAX_WAIT_SECONDS_ENTRYPOINT="${MAX_WAIT_SECONDS_ENTRYPOINT:-60}"
START_TIME="$(date +%s)"

echo "--- 🚀 Avvio Progetto Manage Orders ---"

# Controlli su host, port. Se una delle variabili è vuota, esci.
if [ -z "$DB_HOST" ] || [ -z "$DB_PORT" ]; then
  echo "ERROR: DB_HOST or DB_PORT is empty."
  exit 1
fi

echo "Waiting for MySQL at ${DB_HOST}:${DB_PORT} with user ${DB_USER}..."

command -v getent >/dev/null 2>&1 && getent hosts "$DB_HOST" || true

while true;
do
  NOW_TIME="$(date +%s)"
  ELAPSED="$((NOW_TIME - START_TIME))"
  if [ "$ELAPSED" -ge "$MAX_WAIT_SECONDS_ENTRYPOINT" ]; then
    echo "ERROR: MySQL not reachable after ${MAX_WAIT_SECONDS_ENTRYPOINT}s."
    exit 1
  fi

  # Test apertura porta via /dev/tcp
  if (echo > "/dev/tcp/${DB_HOST}/${DB_PORT}") >/dev/null 2>&1; then
    break
  fi

  echo "Still waiting... (${ELAPSED}s)"
  sleep 2
done

echo "MySQL is ready."
java -jar target/manage-orders-app.jar