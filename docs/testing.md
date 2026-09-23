# Testing Strategy

## Overview

The project uses multiple testing levels so that business logic, API behavior, service communication, and application wiring can be verified at the appropriate boundary.

The goal is meaningful coverage rather than maximizing a coverage percentage.

## Testing Layers

```text
Unit Tests
    ↓
Web / API Tests
    ↓
Service Integration Tests
    ↓
Local Multi-Service Validation
```

Each level should test behavior not already better covered at another level.

## Unit Tests

Unit tests isolate business logic from external infrastructure using JUnit and Mockito.

Examples include:

* patient creation
* patient updates
* patient deletion
* duplicate email handling
* missing-patient behavior
* Billing client invocation
* propagation of Billing RPC failures

Dependencies such as repositories and downstream clients are mocked when the behavior of those dependencies is not the subject of the test.

## REST Controller Tests

The Patient Service uses MockMvc to verify its REST contract.

Current coverage includes:

* successful CRUD requests
* HTTP status codes
* response payloads
* request validation
* malformed JSON
* unknown JSON fields
* unsupported content types
* invalid UUID parameters
* duplicate email conflicts
* missing resources
* standardized `ProblemDetail` responses

These tests focus on HTTP behavior without requiring external services or a running network server.

## gRPC Tests

### Billing Service

Billing tests verify the implementation of the Billing gRPC contract, including successful calls and gRPC status behavior.

### Patient-to-Billing Integration

The Patient Service includes an integration test for its outgoing Billing gRPC communication.

The test exercises:

```text
REST request
    │
    ▼
PatientController
    │
    ▼
PatientService
    │
    ▼
GrpcBillingClient
    │
    ▼
generated gRPC stub
    │
    ▼
in-process gRPC transport
    │
    ▼
test Billing service
```

A test-only implementation of the Billing contract receives the real protobuf request and allows the test to verify the values transmitted across the gRPC boundary.

The Patient repository is mocked in this test because database persistence is not the behavior being exercised.

Using an in-process transport keeps the test:

* fast
* deterministic
* independent of network ports
* independent of a separately running Billing application

## Local Multi-Service Validation

Docker Compose is used to validate the services together during development.

This verifies behavior not covered by the in-process integration tests, including:

* container startup
* Docker networking
* service hostname resolution
* environment configuration
* real Patient-to-Billing communication between containers

This currently serves as a development smoke test rather than a dedicated end-to-end CI suite.

## Continuous Integration

GitHub Actions runs service-focused validation.

Current workflows include:

* Maven builds
* unit tests
* integration tests
* `clean verify`
* Docker image builds

Individual service workflows are kept independent where possible so unrelated changes do not unnecessarily rebuild the entire system.

Changes to shared contracts or build infrastructure may trigger multiple dependent workflows.

## Testing Guidelines

As additional services are introduced:

* keep unit tests focused on business behavior
* test HTTP contracts at the controller boundary
* test service clients against realistic test implementations when useful
* avoid replacing every dependency with a full external service
* add cross-service tests only where they provide meaningful additional confidence
* keep tests deterministic and suitable for CI
* avoid duplicating identical behavior across multiple test layers

Full end-to-end testing can be introduced later for critical workflows that span several production services.
