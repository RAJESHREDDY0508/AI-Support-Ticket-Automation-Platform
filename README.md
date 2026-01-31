# AI Support Ticket Automation Platform

Monorepo for the AI-powered support ticket automation platform with backend microservices, Kafka event streaming, and PostgreSQL.

## Architecture

```
├── backend/
│   ├── api-contracts/       # Shared DTOs, enums, OpenAPI spec
│   └── services/
│       ├── api-gateway/     # API gateway service
│       └── ticket-service/  # Ticket processing service
├── frontend/                # Web application
└── docker-compose.yml       # Infrastructure + services
```

## API Contracts (Shared Module)

DTOs and enums are shared via `backend/api-contracts` to ensure consistency across services.

**Endpoints:**
| Operation | Method | Path |
|-----------|--------|------|
| Create ticket | POST | `/api/tickets` |
| Get ticket by id | GET | `/api/tickets/{id}` |
| List tickets (paginated) | GET | `/api/tickets?page=0&size=20` |
| Update ticket status (admin) | PATCH | `/api/tickets/{id}/status` |
| Fetch AI suggestion | GET | `/api/tickets/{id}/ai-suggestion` |
| Put AI suggestion (internal) | PUT | `/api/tickets/{id}/ai-suggestion` |
| Approve AI response (admin) | PUT | `/api/tickets/{id}/response-approval` |
| Get similar (placeholder) | GET | `/api/tickets/{id}/similar` |

**Headers:** `Idempotency-Key`, `Correlation-ID`, `X-Admin-User`

**Enums:** `Category`, `Priority`, `Status`, `ConfidenceScore`

**DTOs:** `CreateTicketRequest`, `TicketResponse`, `PageResponse`, `UpdateTicketStatusRequest`, `AiSuggestionRequest`, `AiSuggestionResponse`, `ResponseApprovalRequest`, `ApiError`

**OpenAPI spec:** `backend/api-contracts/src/main/resources/openapi/ticket-api.yaml`

**Build** (from `backend/`):
```bash
gradle :api-contracts:build
gradle :services:api-gateway:bootJar
gradle :services:ticket-service:bootJar
```

## Prerequisites

- [Docker](https://docs.docker.com/get-docker/)
- [Docker Compose](https://docs.docker.com/compose/install/) v2+

## How to Run

### 1. Configure environment

```bash
cp .env.example .env
# Edit .env if you need to override defaults
```

### 2. Start infrastructure (PostgreSQL, Kafka, Zookeeper)

```bash
docker compose up -d
```

This starts:

- **PostgreSQL** on `localhost:5432` (reachable as `postgres` on the compose network)
- **Zookeeper** on `localhost:2181`
- **Kafka** on `localhost:9092` (reachable as `kafka` on the compose network)

### 3. Verify services are healthy

```bash
docker compose ps
```

All infra containers should show `healthy` status after ~30 seconds.

### 4. (Optional) Start placeholder application services

```bash
docker compose --profile services up -d
```

This builds and runs the placeholder services (api-gateway, ticket-service, frontend).

### 5. Stop everything

```bash
docker compose --profile services down
docker compose down
```

## Environment Variables

See [.env.example](.env.example) for all configurable variables.

## Connecting to PostgreSQL from services

Use hostname `postgres` and port `5432` when connecting from containers on the `app-network`:

```
postgresql://user:password@postgres:5432/ticket_platform
```

## API Gateway (Spring Boot)

The API Gateway is a Spring Boot 3 application using Gradle.

**Build with Docker:**
```bash
docker compose build api-gateway
```

**Run:**
```bash
# Start infra first
docker compose up -d
# Then start the gateway
docker compose --profile services up -d api-gateway
```

**Local build** (requires Java 17+; from `backend/` with multi-module):
```bash
cd backend
gradle :services:api-gateway:bootJar   # or ./gradlew if wrapper exists
java -jar services/api-gateway/build/libs/api-gateway-0.0.1-SNAPSHOT.jar
```

Endpoints:
- `/actuator/health` — Actuator health
- `/api/health` — Custom health check
- `/api/tickets/**` — Proxied to ticket-service
- `/api/echo` — Validation demo (POST with `{"message":"hello"}`)

## AI Worker (Python)

Consumes `ticket.created.v1` from Kafka, calls ticket-service **PUT /internal/tickets/{id}/ai-suggestion** (with shared token), then ticket-service sets status to **AI_PROCESSED** and emits **ticket.ai_processed.v1**.

**Stack:** aiokafka, tenacity, structlog, prometheus-client, httpx

**Run:** `docker compose --profile services up -d ai-worker` (after Kafka + ticket-service). Set `INTERNAL_API_TOKEN` (same as ticket-service) for internal auth.

**Internal endpoint:** `PUT /internal/tickets/{id}/ai-suggestion` — requires header `X-Internal-Token: <INTERNAL_API_TOKEN>`.

**Metrics:** `http://localhost:9090/metrics` (Prometheus)

**Config:** `KAFKA_BOOTSTRAP_SERVERS`, `TICKET_SERVICE_URL`, `INTERNAL_API_TOKEN`, `TOPIC_TICKET_CREATED`, `TOPIC_DLQ`, `KAFKA_GROUP_ID`, `RETRY_*`, `METRICS_PORT`

## Kafka events (outbox pattern)

Ticket service uses an **outbox table** so ticket creation, AI suggestion, and response approval are written to the DB and outbox in the **same transaction**. A background publisher polls the outbox and publishes to Kafka. If Kafka is down, events stay in the outbox and are published after Kafka is back (with retries; after max retries they go to the DLQ).

**Topics:** `ticket.created.v1`, `ticket.ai_processed.v1`, `ticket.response_approved.v1`, `ticket.dlq.v1`

**Event envelope:** `eventId`, `eventType`, `schemaVersion`, `occurredAt`, `correlationId`, `payload`

**Config:** `app.outbox.batch-size`, `app.outbox.poll-interval-ms`, `app.outbox.max-retries`

## Connecting to Kafka from services

Use hostname `kafka` and port `29092` for internal broker communication:

```
kafka:29092
```

For clients running on the host, use `localhost:9092`.
