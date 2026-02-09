FROM eclipse-temurin:21-jdk

WORKDIR /manage-orders

# Utente non-root
RUN addgroup --system manage-orders && adduser --system --ingroup manage-orders manage-orders-user

# Presupponendo che ci sia solo una versione nella cartella target
ARG JAR_FILE=target/*.jar

# Copia il file JAR selezionato nella posizione dell'app
COPY ${JAR_FILE} target/app.jar
COPY ./scripts ./scripts

# Permessi
RUN chown -R manage-orders-user:manage-orders . && \
    chmod +x scripts/run.sh

# Imposta il punto di ingresso per eseguire il JAR
ENTRYPOINT ["./scripts/run.sh"]