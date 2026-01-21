# Manage Orders - Progetto Subito

**Manage Orders** è un''applicazione basata su Spring Boot e Java progettato per la gestione sicura e scalabile degli ordini. L'applicazione adotta un approccio **event-driven** tramite Kafka e separa nettamente la persistenza dalla comunicazione esterna tramite l'uso di **DTO** e **Mapper**.

## 🚀 Novità e Miglioramenti Recenti

- **Layer DTO & Mapping:** Implementazione di un layer di Data Transfer Object (`dto`) e relativi `mapper` per disaccoppiare le entità JPA dal contratto delle API e dai messaggi Kafka.
- **Gestione Profili Maven/Spring:** Configurazione avanzata dei profili (`local`, `remote`) con filtraggio delle risorse tramite Maven per una gestione dinamica delle configurazioni tra ambienti di sviluppo e container.
- **Ottimizzazione Docker:** Configurazione di MySQL con supporto per il case-insensitivity (`lower_case_table_names=1`) per garantire la compatibilità dei database Linux/Docker con gli script Liquibase.

## 🛠️ Stack Tecnologico

- **Java 21** & **Spring Boot 4.0.1**
- **Spring Security** (JWT Authentication)
- **Spring Data JPA** (MySQL 8.0)
- **Apache Kafka** (Broker di messaggistica asincrona)
- **Liquibase** (Database Migration & Versioning)
- **MapStruct / Custom Mappers** (Conversione Entity <-> DTO)
- **Docker & Docker Compose** (Infrastruttura containerizzata)
- **Lombok**

## 🏗️ Architettura del Sistema

L'applicazione segue un flusso asincrono per la creazione degli ordini:
1. **REST Controller:** Riceve un `OrderDTO` e lo valida.
2. **Service Layer:** Converte il DTO in `Order` (Entity), aggiorna le disponibilità dei prodotti e arricchisce l'ordine con i dati dell'utente autenticato.
3. **Kafka Producer:** Invia il DTO serializzato al topic `topic-orders`.
4. **Kafka Consumer:** Il `CustomKafkaListener` riceve il batch di ordini e li persiste nel database MySQL.

## 🚦 Guida all'avvio

### Prerequisiti
- Docker e Docker Desktop installati.
- Maven 3.9+ o utilizzo del wrapper `./mvnw`.
- Java 21 installato.
- Postman o un altro client HTTP per testare le API.

### Esecuzione
1. **Compila il progetto** attivando il profilo desiderato (es. local se lo si vuole provare senza Docker. Se non si specifica il profilo oppure si sceglie il profilo remote, l'applicazione deve essere eseguita in un container Docker):
   ```bash
   ./mvnw clean package -Plocal -DskipTests
   ```

2. **Avvia l'infrastruttura con Docker Compose:**
   ```bash
   docker compose up --build
   ```

L'applicazione sarà disponibile all'indirizzo `http://localhost:8081/progetto-subito`.

## 🛡️ API Endpoints

- **Autenticazione:** `/auth/login`, `/auth/register-user`
- **Catalogo:** `/api/products/get-all` (Popolato da Liquibase all'avvio)
- **Ordini:**
   - `POST /api/orders/sendOrder`: Invio ordine asincrono.
   - `GET /api/orders/my-orders`: Recupero ordini dell'utente corrente.

---
*Sviluppato da Antonio Basileo*