# Local Development

## Overview

The project supports both Maven-based development and a Docker Compose.
Commands in this document should be run from the repository root unless otherwise noted.

A [Makefile](../Makefile) is provided for common development operations like
environment setup, Maven builds, tests, and Docker Compose operations.

The underlying Maven and Docker commands can still be run directly when finer control is needed.

Run:

```bash
make help
```

to view the available development commands.

## Prerequisites

Install:

* Java 21
* Docker
* Docker Compose
* Make

A local Maven installation is not required because the repository includes the Maven Wrapper.

## Environment Configuration

The project uses a local `.env` file for Docker Compose configuration.

Create it with:

```bash
make env
```

If `.env` does not exist, the command copies `.env.example` to `.env`.

If `.env` already exists, it is left unchanged.

You can also prepare the repository with:

```bash
make setup
```

which creates `.env` if needed and ensures the Maven Wrapper is executable.

Review `.env` before starting the application and update any local values as needed.

For example:

```env
PATIENT_POSTGRES_DB=patient_db
PATIENT_POSTGRES_USER=patient_user
PATIENT_POSTGRES_PASSWORD=your-password
```

The `.env` file is excluded from version control.

## Build the Project

Build and verify all current Maven modules:

```bash
make build
```

This performs a clean Maven build and runs the full verification lifecycle.

For a faster test run without a clean build:

```bash
make test
```

To package the project without running tests:

```bash
make package
```

### Build or Test a Specific Service

Patient Service:

```bash
make verify-patient
```

Billing Service:

```bash
make verify-billing
```

For faster service-specific test runs:

```bash
make test-patient
```

```bash
make test-billing
```

These commands automatically include any required Maven reactor dependencies, such as shared contract modules.

## Start the Application

Build the service images and start the Docker Compose environment:

```bash
make up-build
```

This also creates `.env` from `.env.example` if the file does not already exist.

The current local environment includes:

* Patient Service
* Billing Service
* Patient PostgreSQL database

### Local Ports

| Component          |   Port |
| ------------------ |-------:|
| Patient REST API   | `4000` |
| Billing gRPC       | `9090` |
| Patient PostgreSQL | `5432` |

Patient API:

```text
http://localhost:4000
```

Billing gRPC from the host:

```text
localhost:9090
```

### Start Without Rebuilding

If the Docker images are already built and no rebuild is needed:

```bash
make up
```

### View Running Services

```bash
make ps
```

### View Logs

Follow logs from all Compose services:

```bash
make logs
```

### Restart the Application

```bash
make restart
```

## Stop the Application

Stop and remove the Compose containers:

```bash
make down
```

To also remove Docker volumes:

```bash
make down-volumes
```

> `make down-volumes` permanently deletes data stored in Compose-managed volumes, including local PostgreSQL data.

## Build Docker Images Directly

Build all service images:

```bash
make docker-build
```

Build only the Patient Service image:

```bash
make docker-build-patient
```

Build only the Billing Service image:

```bash
make docker-build-billing
```

Docker builds use the repository root as the build context because services participate in the root Maven reactor and may depend on shared modules.

## API Documentation

With the Patient Service running:

Swagger UI:

```text
http://localhost:4000/swagger-ui.html
```

Scalar:

```text
http://localhost:4000/scalar
```

## Common Development Workflow

A typical development workflow is:

```text
1. Create or switch to a feature branch
2. Make the application changes
3. Run targeted tests during development
4. Run make build before opening a pull request
5. Run make up-build when local multi-service validation is needed
6. Smoke-test the relevant service interaction
7. Commit and open a pull request
```

## Direct Maven and Docker Commands

The Makefile is a convenience layer only. Maven and Docker Compose remain the underlying tools.

Equivalent commands can be run directly when needed.

Full Maven verification:

```bash
./mvnw clean verify
```

Verify Patient Service and its required modules:

```bash
./mvnw \
  -pl patient-service \
  -am \
  clean verify
```

Verify Billing Service and its required modules:

```bash
./mvnw \
  -pl billing-service \
  -am \
  clean verify
```

Start the Docker Compose environment:

```bash
docker compose up --build
```

Stop the Docker Compose environment:

```bash
docker compose down
```
