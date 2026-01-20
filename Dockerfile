FROM eclipse-temurin:21-jdk

RUN useradd -m -d /home/appuser -s /bin/bash appuser
USER appuser

# Presupponendo che ci sia solo una versione nella cartella target
ARG JAR_FILE=target/*.jar

# Copia il file JAR selezionato nella posizione dell'app
COPY ${JAR_FILE} app.jar

# Imposta il punto di ingresso per eseguire il JAR
ENTRYPOINT ["java", "-jar", "/app.jar"]