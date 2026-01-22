#!/bin/bash

# Interrompe lo script in caso di errore
set -e

echo "--- 🚀 Avvio Progetto Subito ---"

# 1. Pulizia e compilazione del pacchetto JAR
echo "--- 📦 Compilazione JAR con Maven (se non specifichiamo il profilo in automatico l'applicazione viene compilata con profilo remote) ---"
./mvnw clean package -DskipTests

# 2. Spegnimento di eventuali container precedenti e pulizia volumi (opzionale)
# Nota: il flag -v cancella i dati del DB, rimuovilo se vuoi mantenere i dati tra i riavvii
echo "--- 🛑 Spegnimento container esistenti ---"
docker compose down

# 3. Creazione cartella per bind mount MySQL (evita errori "no such file or directory")
echo "--- 📁 Creazione silente cartella docker/var/lib/mysql ---"
echo "Step necessario per fare il binding (tramite i volumes) con la cartella /var/lib/mysql nel container MySql"
mkdir -p docker/var/lib/mysql

# 4. Avvio dell'infrastruttura con Docker Compose
echo "--- 🐳 Avvio container con Docker Compose ---"
docker compose up --build

echo "--- ✅ Applicazione avviata con successo! ---"
echo "API disponibili su: http://localhost:8081/progetto-subito"
echo "Log dell'app in corso... (Premi Ctrl+C per uscire dai log, i container rimarranno attivi)"

# Mostra i log dell'applicazione
docker logs -f progetto-subito-app