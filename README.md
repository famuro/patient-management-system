# Patient Management System

A microservices-based patient management platform built with Spring Boot.

The project is designed to demonstrate the architecture and development of a production-style distributed system using independently deployable services, synchronous and asynchronous service communication, authentication, event-driven processing, and modern deployment practices.

> **Status:** Initial development

## Overview

The Patient Management System will consist of several independently deployable services responsible for distinct areas of the application.

Planned services include:

* **Patient Service** — manages patient information and patient-related operations.
* **Auth Service** — handles authentication and authorization.
* **Billing Service** — manages billing-related operations.
* **Analytics Service** — processes application events for reporting and analytics.
* **Notification Service** — handles application notifications.
* **API Gateway** — provides a centralized entry point for external API requests.

## Architecture

The application will follow a microservices architecture with service boundaries based on business responsibilities.

Planned communication patterns include:

* REST APIs for external client communication
* gRPC for synchronous internal communication between the Patient and Billing services
* Apache Kafka for asynchronous event-driven communication between services
* An API Gateway for routing and centralized request handling

Each service will be designed to remain independently buildable, testable, and deployable.

## Technology Stack
Currently implemented:

- Java
- Spring Boot
- Spring Data JPA
- Springdoc OpenAPI / Swagger UI
- Maven
- H2
- JUnit
- Mockito
- GitHub Actions

Planned as the system expands:

- Spring Security
- Spring Cloud Gateway
- PostgreSQL
- Docker
- gRPC
- Apache Kafka

Additional infrastructure, observability, testing, and deployment tooling will be introduced as the project evolves.

## Repository Structure

This project uses a monorepo containing independently deployable microservices.

```text
patient-management-system/
├── api-gateway/
├── auth-service/
├── patient-service/
├── billing-service/
├── analytics-service/
├── notification-service/
├── infrastructure/
└── docs/
```

The repository structure will be expanded as services and supporting infrastructure are implemented.

## API Documentation

The Patient Service includes OpenAPI documentation generated with Springdoc.
When the service is running locally, interactive API documentation is available through Swagger UI:
```text
localhost:4000/swagger-ui.html
```
or through Scalar:
```text
localhost:4000/scalar
```
The documentation includes the available patient endpoints, request operations, and API descriptions defined alongside the controller implementation. 
OpenAPI annotations are maintained with the application code so that the API documentation evolves with the service.

## Development

The project is under active development.

The Patient Service currently provides REST operations for creating, retrieving, updating, and deleting patient records, along with request validation, centralized exception handling, automated tests, and continuous integration through GitHub Actions.

Additional services and infrastructure will be introduced incrementally as development continues.


## License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.
