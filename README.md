# Manage Orders 

**Manage Orders** è un'applicazione basata su Spring Boot e Java progettata per la gestione sicura e scalabile degli ordini. L'applicazione adotta un approccio **event-driven** tramite Kafka e separa nettamente la persistenza dalla comunicazione esterna tramite l'uso di **DTO** e **Mapper**.

## Novita e miglioramenti recenti

Negli ultimi aggiornamenti il progetto e' stato reso piu' modulare e piu' semplice da gestire tra ambienti locali e deploy remoto.

- **Layer DTO e Mapper:** introduzione di `dto` e `mapper` per disaccoppiare entita' JPA, API REST e payload Kafka.
- **Profili Maven/Spring:** separazione dei profili `local` e `remote` con filtraggio risorse Maven per parametrizzare configurazioni e bootstrap.
- **Compatibilita' Docker/MySQL:** tuning di MySQL (incluso `lower_case_table_names=1`) per evitare mismatch di naming con Liquibase in ambienti Linux/container.

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
4. **Kafka Consumer:** Il `CustomKafkaListener` riceve il messaggio e lo persiste nel database MySQL.

### Struttura Kafka del progetto

La configurazione Kafka e' organizzata con responsabilita' separate:

- `KafkaProducerConfig`: costruisce il `ProducerFactory` e il `KafkaTemplate` (`retryableTopicKafkaTemplate`) usando le proprieta' `spring.kafka.producer.*`.
- `KafkaConsumerConfig`: crea `ConsumerFactory` e `manageOrdersListenerContainerFactory`, con supporto a concorrenza (nel profilo local e' impostata a `3`).
- `KafkaRetryConfig`: configura retry non-blocking con backoff esponenziale e pubblicazione automatica su topic di retry e DLT.

Topic e flusso messaggi:

- **Topic principale:** `topic-orders` (input ordini).
- **Topic retry:** creati automaticamente con suffisso `-retry`.
- **Dead Letter Topic (DLT):** creata automaticamente con suffisso `-dlt`.
- **Consumer Group:** `manage-orders`.

Flusso applicativo dettagliato:

1. Il controller invia la richiesta al service (`OrderService`).
2. Il service pubblica un evento Avro su `topic-orders` tramite `KafkaTemplate`.
3. `CustomKafkaListener` consuma dal topic principale con `manageOrdersListenerContainerFactory`.
4. Se la consumazione fallisce con eccezioni gestite, Spring Kafka inoltra il record sui topic `-retry` secondo la policy esponenziale.
5. Al superamento dei tentativi, il messaggio viene inviato al topic `-dlt`.
6. Il metodo `@DltHandler` salva il contenuto del messaggio non processato nella tabella di dead letter (`manage_orders_dead_letter`).

Disegno architetturale (semplificato):

```text
Client REST
    |
    v
OrderController -> OrderService -> Kafka Producer (KafkaTemplate)
                                   |
                                   v
                           topic-orders (main)
                                   |
                                   v
                    CustomKafkaListener (consumer group: manage-orders)
                         | successo                    | errore retryable
                         v                             v
                    MySQL (orders)            topic-orders-retry-* (backoff)
                                                      |
                                                      v
                                            topic-orders-dlt
                                                      |
                                                      v
                                  @DltHandler -> MySQL (dead letter)
```

Note operative:

- In ambiente single-broker (es. Docker Desktop/Kubernetes locale), la replica dei topic deve essere coerente con il numero di broker disponibili.
- Il producer/consumer usa serializzazione Avro (con Schema Registry configurato via `spring.kafka.properties['schema.registry.url']`).

## 🚦 Guida all'avvio

### Prerequisiti
- Docker e Docker Desktop installati.
- Maven 3.9+ o utilizzo del wrapper `./mvnw`.
- Java 21 installato.
- Postman o un altro client HTTP per testare le API.

### Pipeline CI/CD con GitLab

Il progetto è configurato con una **GitLab CI/CD Pipeline** definita nel file `.gitlab-ci.yml`.
La pipeline è eseguita automaticamente ad ogni push o merge request effettuati sul branch `main` tramite **GitLab Runner**.

#### Struttura della Pipeline
- **Build Stage:** Compilazione del progetto Maven e esecuzione dei test.
- **Deploy Stage:** Deployment automatico su Kubernetes tramite `kubectl`.

#### Configurazione del Runner
I runner GitLab sono configurati per eseguire i job della pipeline in ambienti containerizzati. Assicurati che:
1. Il runner GitLab sia installato e registrato nel progetto.
2. Docker sia disponibile nell'ambiente di esecuzione del runner.
3. Il contesto Kubernetes (`docker-desktop`) sia configurato correttamente.


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