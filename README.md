# Patient Management System

A production-oriented microservices backend built with **Java 21** and **Spring Boot 4**.

The project demonstrates the incremental development of a distributed system using REST APIs, gRPC service communication, PostgreSQL, Docker, automated testing, and CI/CD.

> **Status:** Active development

## Architecture

Currently implemented:

```text
Client
  |
  | REST
  v
Patient Service --> PostgreSQL
  |
  | gRPC
  v
Billing Service
```

When a patient is created, the Patient Service persists the patient and synchronously requests creation of a corresponding Billing account over gRPC.

A shared Protocol Buffers module defines the contract between the two services.

### Current Services

* **Patient Service** — REST API for patient management
* **Billing Service** — internal gRPC service for billing account creation
* **Billing Contract** — shared protobuf/gRPC contract

Planned:

* Auth Service
* API Gateway
* Analytics Service
* Notification Service
* Apache Kafka for event-driven communication

## Tech Stack

**Backend**

* Java 21
* Spring Boot 4
* Spring Web MVC
* Spring Data JPA
* Spring gRPC
* Protocol Buffers

**Data**

* PostgreSQL

**Testing**

* JUnit 5
* Mockito
* MockMvc
* Spring Boot integration tests
* in-process gRPC integration testing

**Infrastructure**

* Maven multi-module build
* Docker + Compose
* GitHub Actions

**API Documentation**

* OpenAPI
* Swagger UI
* Scalar

## Repository Structure

```text
patient-management-system/
├── .github/
│   └── workflows/
├── contracts/
│   └── billing-contract/
├── patient-service/
├── billing-service/
├── docker-compose.yaml
├── pom.xml
└── README.md
```

The project uses a Maven multi-module monorepo while keeping services independently deployable.

## Patient API

| Method   | Endpoint                | Description           |
| -------- | ----------------------- | --------------------- |
| `GET`    | `/api/v1/patients`      | Retrieve all patients |
| `POST`   | `/api/v1/patients`      | Create a patient      |
| `GET`    | `/api/v1/patients/{id}` | Retrieve a patient    |
| `PUT`    | `/api/v1/patients/{id}` | Update a patient      |
| `DELETE` | `/api/v1/patients/{id}` | Delete a patient      |

REST errors use Spring `ProblemDetail` to provide a consistent RFC 9457-style error response.

## Running Locally

### Prerequisites

* Docker
* Docker Compose

Create a local `.env` file from `.env.example`, then run:

```bash
docker compose up --build
```

This starts:

* Patient Service
* Billing Service
* Patient PostgreSQL database

Patient Service:

```text
http://localhost:4000
```

Billing gRPC:

```text
localhost:9090
```

Stop the environment with:

```bash
docker compose down
```

To also remove persisted PostgreSQL data:

```bash
docker compose down -v
```

## Building and Testing

Run the complete Maven build:

```bash
./mvnw clean verify
```

Build and test a specific service with its required modules:

```bash
./mvnw -pl patient-service -am clean verify
```

```bash
./mvnw -pl billing-service -am clean verify
```

GitHub Actions independently validates the services with Maven tests and Docker image builds.

## API Documentation

With the Patient Service running:

**Swagger UI**

```text
http://localhost:4000/swagger-ui.html
```

**Scalar**

```text
http://localhost:4000/scalar
```

## Documentation

Additional design and implementation details:

* [Architecture](docs/architecture.md)
* [gRPC Communication](docs/grpc.md)
* [Testing Strategy](docs/testing.md)
* [Local Development](docs/local-development.md)
* [Roadmap](docs/roadmap.md)

## Roadmap

Next major areas of development include:

* Apache Kafka and event-driven communication
* authentication and authorization
* API Gateway
* Analytics and Notification services
* Billing persistence
* resilience and observability
* Kubernetes deployment

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.
