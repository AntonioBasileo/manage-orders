#!/bin/bash

# Interrompe lo script in caso di errore
set -e

cd /manage-orders

DB_HOST="${DB_HOST:-db-service.general.svc.cluster.local}"
DB_PORT="${DB_PORT:-3306}"
DB_NAME="${DB_NAME:-manage-orders}"
DB_USER="${SPRING_DATASOURCE_USERNAME:-}"
DB_PASSWORD="${DB_PASSWORD:-}"
MAX_WAIT_SECONDS_ENTRYPOINT="${MAX_WAIT_SECONDS_ENTRYPOINT:-60}"
START_TIME="$(date +%s)"

echo "--- 🚀 Avvio Progetto Manage Orders ---"

# Controlli su host, port, dbname e user. Se una delle variabili è vuota, esci.
if [ -z "$DB_HOST" ] || [ -z "$DB_PORT" ] || [ -z "$DB_NAME" ] || [ -z "$DB_USER" ]; then
  echo "ERROR: DB_HOST or DB_PORT or DB_NAME or DB_USER is empty."
  exit 1
fi

export DB_HOST
export DB_PORT
export DB_USER
export DB_PASSWORD
export DB_NAME

echo "Waiting for MySQL at ${DB_HOST}:${DB_PORT} with user ${DB_USER}..."

# Crea un file Java temporaneo per testare la connessione
cat > /tmp/TestDBConnection.java <<'JAVA_CODE'
import java.sql.*;
public class TestDBConnection {
    public static void main(String[] args) {
        String host = System.getenv("DB_HOST");
        String port = System.getenv("DB_PORT");
        String db = System.getenv("DB_NAME");
        String user = System.getenv("DB_USER");
        String password = System.getenv("DB_PASSWORD");

        String jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + db
            + "?allowPublicKeyRetrieval=true&useSSL=false";

        try (Connection conn = DriverManager.getConnection(jdbcUrl, user, password);
             PreparedStatement stmt = conn.prepareStatement("SELECT 1");
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                System.exit(0);
            } else {
                System.exit(1);
            }
        } catch (Exception e) {
            System.exit(1);
        }
    }
}
JAVA_CODE

# Trova il MySQL connector JAR
MYSQL_JAR=$(find target -name 'mysql-connector-j-*.jar' | head -1)

until javac /tmp/TestDBConnection.java && java -cp "/tmp:$MYSQL_JAR" TestDBConnection 2>/dev/null
do
  NOW_TIME="$(date +%s)"
  ELAPSED="$((NOW_TIME - START_TIME))"
  if [ "$ELAPSED" -ge "$MAX_WAIT_SECONDS_ENTRYPOINT" ]; then
    echo "ERROR: MySQL not ready after ${MAX_WAIT_SECONDS_ENTRYPOINT}s."
    exit 1
  fi
  sleep 1
done

echo "MySQL is ready. Running migrations..."
java -jar target/app.jar