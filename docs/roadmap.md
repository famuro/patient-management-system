# Roadmap

## Overview

The Patient Management System is being developed incrementally from a small set of working services into a broader distributed system.

The roadmap is directional rather than a fixed delivery schedule. Features may move between phases as architectural requirements become clearer.

Completed work remains documented in the repository history and pull requests rather than turning this file into a detailed changelog.

## Current Foundation

The project currently includes:

* Java 21 and Spring Boot 4
* Maven multi-module monorepo
* Patient REST API
* PostgreSQL persistence
* standardized `ProblemDetail` API errors
* OpenAPI, Swagger UI, and Scalar documentation
* Billing gRPC service
* shared Protocol Buffers Billing contract
* Patient-to-Billing gRPC client integration
* unit, controller, and gRPC integration testing
* Docker and Docker Compose
* GitHub Actions service-level CI
* multi-stage Docker builds

## Near-Term

### Event-Driven Communication

Introduce Apache Kafka and establish the project's asynchronous communication model.

Expected areas include:

* application event publishing
* event consumers
* event contracts
* analytics processing
* notification workflows

### Additional Services

Introduce services as their responsibilities become relevant:

* Auth Service
* API Gateway
* Analytics Service
* Notification Service

Each service should remain focused on a clear domain or infrastructure responsibility.

### Database Evolution

Improve database lifecycle management with:

* schema migrations
* repeatable local database setup
* production-oriented migration practices

## Service Resilience

As synchronous communication grows, introduce appropriate resilience mechanisms such as:

* gRPC deadlines
* failure translation
* retry policies where safe
* health checks
* graceful service degradation

Distributed consistency strategies will be chosen based on concrete workflows rather than applied globally.

## Security

Introduce authentication and authorization through the Auth Service and API Gateway.

Expected areas include:

* Spring Security
* authenticated requests
* authorization rules
* token handling
* service boundary security

## Observability

Introduce operational visibility as the number of services grows.

Potential additions include:

* structured logging
* metrics
* distributed tracing
* service health information
* correlation or trace identifiers

The exact tooling will be selected when the system is complex enough to benefit from centralized observability.

## Testing Evolution

Expand testing where additional confidence is justified.

Potential additions include:

* database integration tests
* Testcontainers
* Kafka integration tests
* critical multi-service workflow tests
* contract compatibility testing

The project will continue to favor targeted tests over duplicating the same behavior across every testing layer.

## Deployment

Later deployment work may include:

* production container configuration
* image publishing
* Kubernetes
* AWS
* deployment pipelines
* environment-specific configuration
* secrets management
