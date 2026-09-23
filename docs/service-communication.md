# Service Communication

## Overview

Services communicate using different mechanisms depending on the requirements of the interaction.

The project currently uses:

* REST for external API communication
* gRPC for synchronous internal service communication
* Protocol Buffers for shared gRPC contracts

Apache Kafka is planned for asynchronous workflows and application events.

## Communication Strategy

```text
External synchronous communication
    → REST

Internal synchronous communication
    → gRPC

Asynchronous service communication
    → Kafka
```

The communication mechanism should match the behavior required by the use case rather than applying one technology to every interaction.

## REST

The Patient Service currently exposes the external REST API.

Patient endpoints are versioned under:

```text
/api/v1/patients
```

REST is appropriate at this boundary because it provides a broadly compatible HTTP API for external consumers and integrates naturally with OpenAPI documentation.

The Patient Service uses Spring `ProblemDetail` for standardized REST error responses.

## gRPC

gRPC is currently used between the Patient and Billing services.

### Patient Creation Flow

```text
POST /api/v1/patients
        │
        ▼
PatientController
        │
        ▼
PatientService
        │
        ├──────────> PatientRepository
        │
        ▼
GrpcBillingClient
        │
        ▼
BillingServiceBlockingStub
        │
       gRPC
        │
        ▼
Billing Service
```

After a patient is persisted, the Patient Service synchronously requests creation of the corresponding Billing account.

### Shared Contract

The Billing contract is maintained in:

```text
contracts/billing-contract/
```

The Protocol Buffers definition is the source of truth for the gRPC API.

Both Patient and Billing depend on the generated contract classes.

Neither service depends on the other's implementation.

### Patient gRPC Client

The Patient Service wraps the generated Billing stub in `GrpcBillingClient`.

```text
PatientService
      │
      ▼
GrpcBillingClient
      │
      ▼
BillingServiceBlockingStub
```

This isolates protobuf and transport-specific code from the Patient Service's core business logic.

Spring manages the gRPC client and channel configuration.

The Billing target is supplied through application configuration so different environments can use different service locations without changing Java code.

### Current Failure Behavior

Patient creation currently performs Billing account creation synchronously.

The simplified sequence is:

```text
persist patient
      │
      ▼
call Billing
      │
      ├── success → return created patient
      │
      └── failure → propagate gRPC failure
```

This behavior is intentionally simple during the initial synchronous integration.

More advanced distributed consistency strategies may be introduced later when the system requirements justify them.

Potential future approaches include:

* request deadlines
* retry policies
* compensating operations
* asynchronous account creation
* event-driven workflows

## Kafka

Apache Kafka is planned for interactions that do not require an immediate synchronous response.

Likely use cases include:

```text
Patient Service
      │
      │ PatientCreated
      ▼
    Kafka
      │
      ├────────> Analytics Service
      │
      └────────> Notification Service
```

Kafka will allow downstream consumers to react independently without introducing direct runtime dependencies between the producing service and every consumer.

Event schemas and ownership conventions will be documented when Kafka is introduced.

## API Gateway

A future API Gateway will provide the primary external entry point to the system.

Expected responsibilities include:

* request routing
* authentication integration
* centralized external API access
* selected cross-cutting request concerns

The gateway should route requests to services rather than absorb their domain logic.

## Contract Ownership

Communication contracts should live with the boundary they define rather than inside unrelated service implementation code.

Current example:

```text
contracts/
└── billing-contract/
```

Additional shared contracts may be introduced when new service-to-service interfaces require them.

Contract modules should remain focused and should not become a general-purpose shared application library.

## Design Guidelines

As the system grows:

* prefer REST at external HTTP boundaries
* use gRPC where synchronous internal RPC provides a clear benefit
* use events when consumers do not require an immediate response
* avoid direct database access between services
* avoid sharing service implementation classes
* keep contracts explicit and versionable
* keep transport concerns outside core business logic where practical