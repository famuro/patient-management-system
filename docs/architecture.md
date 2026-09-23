# Architecture

## Overview

The Patient Management System is a Spring Boot microservices project built as a Maven multi-module monorepo.

The system is developed incrementally, with each service responsible for a distinct business capability. Services communicate through explicit REST, gRPC, or event-driven contracts rather than depending on each other's internal implementations.

The current system includes the Patient and Billing services, with additional services and infrastructure planned as the application grows.

## Current Architecture

```text
           REST
Client ───────────> Patient Service
                          │
                          │ gRPC
                          ▼
                    Billing Service
                                     
Patient Service
      │
      ▼
 PostgreSQL
```

### Implemented Components

| Component        | Responsibility                             | Interface        |
| ---------------- | ------------------------------------------ | ---------------- |
| Patient Service  | Patient lifecycle and external patient API | REST             |
| Billing Service  | Billing account operations                 | gRPC             |
| Billing Contract | Shared Patient-to-Billing service contract | Protocol Buffers |
| Patient Database | Patient persistence                        | PostgreSQL       |

### Planned Components

| Component            | Responsibility                                           |
| -------------------- | -------------------------------------------------------- |
| API Gateway          | External request routing and centralized API entry point |
| Auth Service         | Authentication and authorization                         |
| Analytics Service    | Event-driven analytics and reporting                     |
| Notification Service | User and system notifications                            |
| Apache Kafka         | Asynchronous event distribution between services         |

These components will be introduced as their responsibilities become necessary rather than being added upfront.

## Repository Architecture

The project uses a monorepo with a root Maven reactor.

```text
patient-management-system/
├── contracts/
│   └── billing-contract/
├── patient-service/
├── billing-service/
├── docs/
├── docker-compose.yaml
└── pom.xml
```

Services remain independently deployable even though their source code is maintained in the same repository.

Shared service contracts are maintained separately under `contracts/`.

## Module Dependencies

The current Maven dependency relationship is:

```text
                 billing-contract
                    ▲       ▲
                    │       │
          patient-service   billing-service
```

Both services depend on the Billing contract.

The Patient Service does **not** depend on the Billing Service implementation.

This keeps the service boundary explicit and allows either service to change internally without affecting the other as long as the shared contract remains compatible.

## Service Responsibilities

### Patient Service

The Patient Service currently owns:

* patient creation
* patient retrieval
* patient updates
* patient deletion
* request validation
* patient persistence
* standardized REST error handling
* initiation of Billing account creation

It currently provides the primary external REST API.

### Billing Service

The Billing Service currently owns:

* its gRPC service interface
* Billing account creation
* generation of Billing account identifiers
* Billing account status responses

Billing persistence and additional Billing domain behavior will be introduced as the Billing domain expands.

## Data Ownership

Each service should own the data associated with its domain.

Currently, the Patient Service owns its PostgreSQL database.

As additional stateful services are introduced, they should manage their own persistence rather than accessing another service's database directly.

Service-to-service access should occur through defined contracts or events.

## Architectural Principles

The project follows several guiding principles:

* services are organized around business capabilities
* service implementation details remain private to the owning service
* inter-service communication uses explicit contracts
* databases are owned by the services responsible for their data
* synchronous and asynchronous communication are chosen based on the use case
* infrastructure is introduced when it solves a concrete requirement
* services should remain independently buildable, testable, and deployable

## Evolution

The architecture will continue to evolve as new capabilities are introduced.

Major upcoming changes include:

```text
Client
  │
  ▼
API Gateway
  │
  ├──────────────> Auth Service
  │
  ▼
Patient Service ─── gRPC ───> Billing Service
  │
  │ events
  ▼
Kafka
  │
  ├──────────────> Analytics Service
  │
  └──────────────> Notification Service
```

This diagram represents the planned direction rather than the current deployed system.

Implementation progress is tracked in [roadmap.md](roadmap.md).
