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

## Planned Technology Stack

* Java
* Spring Boot, Security, Cloud Gateway
* Maven
* Apache Kafka
* gRPC
* PostgreSQL
* Docker
* GitHub Actions

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

## Development

The project is currently under active development.

Setup and local development instructions will be added as the application infrastructure is introduced.

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.
