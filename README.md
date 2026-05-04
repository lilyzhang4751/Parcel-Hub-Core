
# Parcel-Hub-Core

A production-oriented parcel hub backend built with Java and Spring Boot.  
The project demonstrates REST API design, authentication, authorization, persistence, asynchronous messaging, observability, containerized deployment, and CI quality checks in a compact logistics domain.

## Purpose

This project is not a toy CRUD demo. It is designed as an interview-ready backend service that shows how a parcel station system can be structured as a maintainable modular monolith before a possible microservice split.

Core domain capabilities:

- Station registration
- User registration and login
- JWT-based authentication
- Role-based authorization
- Parcel inbound preparation
- Parcel inbound confirmation
- Parcel outbound / return / shelf transfer operations
- Parcel query APIs
- Parcel notification flow
- Transactional outbox for reliable event publishing
- PostgreSQL persistence with Flyway migrations
- Redis / Redisson integration
- Kafka-based asynchronous processing
- Actuator, Prometheus, and Grafana observability
- Docker Compose local runtime
- GitHub Actions CI workflow
- Testcontainers-based integration testing

## Tech Stack

| Area | Technology |
|---|---|
| Language | Java 17+ |
| Framework | Spring Boot |
| API | Spring WebMVC, Spring Validation, SpringDoc OpenAPI |
| Security | Spring Security, JWT, BCrypt |
| Persistence | PostgreSQL, Spring Data JPA, Flyway |
| Messaging | Apache Kafka |
| Cache / Locking | Redis, Redisson |
| Observability | Spring Boot Actuator, Micrometer, Prometheus, Grafana |
| Testing | JUnit 5, Spring Boot Test, Testcontainers |
| Build | Maven |
| CI/CD | GitHub Actions, Docker, GitHub Container Registry |
| Runtime | Docker Compose |

## Architecture

```text
Client / Postman / Swagger UI
        |
        v
REST Controllers
        |
        v
Security Filter Chain
JWT Authentication + Role Authorization
        |
        v
Application Services
Use-case orchestration, transaction boundary, validation
        |
        v
Domain Logic
Parcel state transition, operation rules, station/user constraints
        |
        v
Infrastructure
JPA Repositories | Redis / Redisson | Kafka Producer / Consumer | Outbox Publisher
        |
        v
PostgreSQL | Redis | Kafka
```

The codebase follows a modular monolith structure. Business modules are separated by responsibility, and each module is internally layered.

```text
src/main/java/com/lily/parcelhubcore
├── parcel
│   ├── api
│   │   ├── controller
│   │   ├── request
│   │   └── response
│   ├── application
│   │   ├── command
│   │   ├── dto
│   │   ├── query
│   │   └── service
│   ├── common
│   ├── domain
│   └── infrastructure
│       ├── kafka
│       └── persistence
├── user
│   ├── api
│   ├── application
│   ├── common
│   ├── infrastructure
│   └── shared
└── shared
    ├── authentication
    ├── cache
    ├── constants
    ├── exception
    ├── filter
    ├── handler
    ├── lock
    ├── response
    ├── util
    └── validate
```

## Key Engineering Decisions

### Modular Monolith First

The project is intentionally implemented as a modular monolith instead of prematurely splitting into microservices.

Reasoning:

- Shared database transactions are still useful in this stage.
- Module boundaries are visible in package structure.
- Future service extraction is possible around `user`, `station`, `parcel`, and `notification`.
- Operational complexity is kept reasonable for an interview project.

### Transactional Outbox

Parcel operations that need asynchronous side effects are persisted together with an outbox record in the same database transaction.

```text
Business transaction
    ├── update parcel state
    ├── insert parcel operation record
    └── insert outbox event
              |
              v
Outbox polling publisher
              |
              v
Kafka
              |
              v
Consumer-side processing
```

This avoids the classic inconsistency problem:

```text
Database transaction committed, but Kafka publish failed.
```

### Security Model

The system uses:

- JWT for stateless authentication
- BCrypt for password hashing
- Simple role-based authorization
- `MANAGER` and `STAFF` authority checks
- Current user context extracted from authenticated request

This is deliberately simple but realistic enough for backend interviews.

### Observability

The application exposes operational endpoints for local and containerized environments:

- `/actuator/health`
- `/actuator/metrics`
- `/actuator/prometheus`
- `/v3/api-docs`
- `/swagger-ui.html`

Prometheus and Grafana are included in the Docker Compose setup.

## API Overview

Detailed request and response schemas are available through Swagger UI after startup.

### Public APIs

| Method | Path | Description |
|---|---|---|
| POST | `/station/register` | Register a parcel station |
| POST | `/user/register` | Register a user |
| POST | `/user/login` | Login and receive JWT |
| GET | `/user/logout` | Logout |

### Parcel Operation APIs

Require `MANAGER` or `STAFF`.

| Method | Path | Description |
|---|---|---|
| POST | `/parcel/prepare/in` | Prepare parcel inbound operation |
| POST | `/parcel/inbound` | Confirm parcel inbound |
| POST | `/parcel/outbound` | Mark parcel as outbound |
| POST | `/parcel/returned` | Mark parcel as returned |
| POST | `/parcel/transfer` | Transfer parcel to another shelf |

### Parcel Query APIs

Require `MANAGER` or `STAFF`.

| Method | Path | Description |
|---|---|---|
| GET | `/parcels` | Query parcels by page and filter conditions |
| GET | `/parcels/{waybillCode}` | Query parcel detail by waybill code |

### Notification APIs

Require `MANAGER`.

| Method | Path | Description |
|---|---|---|
| GET | `/notify/sms/{waybillCode}` | Trigger parcel SMS notification flow |

## Local Development

### Prerequisites

- JDK 21 or compatible JDK
- Maven Wrapper included in the repository
- Docker
- Docker Compose

### Start Full Local Environment

```bash
docker compose up --build
```

Services:

| Service | URL |
|---|---|
| Application | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Actuator Health | http://localhost:8080/actuator/health |
| Prometheus | http://localhost:9090 |
| Grafana | http://localhost:3000 |

### Run Tests

```bash
./mvnw -B clean verify
```

The test workflow is designed to run:

- unit tests
- Spring Boot integration tests
- Testcontainers-based database / infrastructure tests
- Maven Failsafe integration-test phase

Docker must be available when running integration tests that rely on Testcontainers.

### Package Application

```bash
./mvnw clean package -DskipTests
```

### Build Docker Image

```bash
docker build -t parcel-hub-core:local .
```

## Configuration

The application is configured through Spring profiles and environment variables.

Important configuration groups:

| Group | Purpose |
|---|---|
| `spring.datasource.*` | PostgreSQL connection |
| `spring.data.redis.*` | Redis connection |
| `spring.kafka.*` | Kafka producer / consumer settings |
| `app.crypto.*` | Sensitive field encryption and hashing |
| `jwt.secret` | JWT signing secret |
| `app.outbox.*` | Outbox polling, retry, and recovery settings |
| `management.*` | Actuator and Prometheus exposure |

For production-like usage, do not reuse development secrets. Provide secrets through environment variables, CI/CD secrets, or a secret manager.

## CI/CD

The repository contains GitHub Actions workflows for:

- running tests on push and pull request
- packaging the application
- building and pushing Docker images to GitHub Container Registry
- deployment-oriented Compose configuration

Typical CI command:

```bash
mvn -B clean verify
```

Image build workflow:

```text
push to main
    |
    v
Maven package
    |
    v
Docker build
    |
    v
Push image to GHCR
```

## Database Migration

Database schema is managed by Flyway.

```text
src/main/resources/db/migration
└── V1__init.sql
```

The application uses:

```properties
spring.jpa.hibernate.ddl-auto=validate
spring.flyway.enabled=true
```

This means schema changes should be made through migration scripts, not by automatic Hibernate schema generation.

## Testing Strategy

The project uses several test levels:

| Test Type | Purpose |
|---|---|
| Unit tests | Validate isolated business logic |
| Slice tests | Validate repository / web / security components where applicable |
| Integration tests | Validate application behavior with real infrastructure |
| Testcontainers | Run PostgreSQL / Kafka-dependent tests against real containers |

The goal is not only code coverage, but confidence that the service works with realistic external dependencies.

## Scope Boundaries

This project focuses on backend engineering quality rather than complete logistics business coverage.

Current intentional boundaries:

- simplified role model
- simplified station and user model
- no external SMS provider integration required for local demonstration
- single-service deployment by default
- production hardening demonstrated through patterns, not full cloud infrastructure

## Possible Next Improvements

- Add OpenTelemetry tracing with Tempo or Jaeger
- Add centralized structured logging
- Add idempotency keys for selected external-facing APIs
- Add more explicit domain state machine tests
- Add API contract tests
- Add Kubernetes manifests or Helm chart
- Extract notification processing as a separate service
- Add database indexes based on query plans and load-test results

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.