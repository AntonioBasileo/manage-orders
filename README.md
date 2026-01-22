# Manage Orders - Progetto Subito

**Manage Orders** è un'applicazione basata su Spring Boot e Java progettata per la gestione sicura e scalabile degli ordini. L'applicazione adotta un approccio **event-driven** tramite Kafka e separa nettamente la persistenza dalla comunicazione esterna tramite l'uso di **DTO** e **Mapper**.

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
1. Eseguire lo script run.sh che troverete nella cartella **scripts**.

L'applicazione sarà disponibile all'indirizzo `http://localhost:8081/progetto-subito`.

Essendo stato effettuato un binding tra la cartella docker/var/lib/mysql (creata nella root del progetto con lo script run.sh) e la cartella /var/lib/mysql del container MySQL,
potete fare un check sull'integrità dei dati stabilendo una connessione MySql con strumenti come Dbeaver, DataGrip ecc.. (host della connessione: localhost, porta: 3307)

## 🛡️ API Endpoints & Autenticazione

Tutte le API dell'applicazione (eccetto quelle di registrazione e login) sono protette tramite **JSON Web Token (JWT)**. Per testare le funzionalità, segui questa procedura:

1. **Registrazione:** Crea un nuovo account inviando una richiesta POST a `/auth/register-user` con il JSON contenente `username`, `password` e `role`. Il role specifica il ruolo dell'utente (es. `ROLE_USER` o `ROLE_ADMIN`)
ed è importante perchè, ad esempio, aggiungere un nuovo prodotto è una funzionalità concessa solo all'admin.
2. **Login:** Ottieni il token inviando una richiesta GET a `/auth/login` con le medesime credenziali. Il sistema restituirà un JSON contenente il campo `token`.
3. **Autorizzazione:** Copia il token ricevuto e inseriscilo nell'header di ogni richiesta successiva utilizzando la chiave `Authorization` e il prefisso `Bearer ` (es. `Authorization: Bearer <tuo_token_qui>`).

### Elenco Endpoint:

- **Pubblici:**
    - `POST /auth/register-user`: Registrazione nuovo utente.
    - `GET /auth/login`: Autenticazione e rilascio token JWT.

- **Protetti (Richiedono Header Authorization):**
    - `GET /api/products/get-all`: Visualizza il catalogo prodotti (popolato automaticamente da Liquibase).
    - `POST /api/products/add-product`: Aggiunge un nuovo prodotto al catalogo.
    - `POST /api/orders/sendOrder`: Invia un nuovo ordine (processato in modo asincrono tramite Kafka).
    - `GET /api/orders/my-orders`: Visualizza lo storico ordini dell'utente autenticato.

---
*Sviluppato da Antonio Basileo*