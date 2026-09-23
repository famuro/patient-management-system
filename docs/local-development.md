# Local Development

## Overview

The project supports both Maven-based development and a Docker Compose environment for running multiple services together.

Commands in this document should be run from the repository root unless otherwise noted.

## Prerequisites

Install:

* Java 21
* Docker
* Docker Compose

A local Maven installation is not required because the repository includes the Maven Wrapper.

## Environment Configuration

Copy the example environment file:

```bash
cp .env.example .env
```

Configure the required local values in `.env`.

For example:

```env
PATIENT_POSTGRES_DB=patient_db
PATIENT_POSTGRES_USER=patient_user
PATIENT_POSTGRES_PASSWORD=your-password
```

The `.env` file is excluded from version control.

A Makefile is also provided for common development commands, including environment setup.

## Build the Project

Build and verify all current Maven modules:

```bash
./mvnw clean verify
```

## Build a Specific Service

Patient Service:

```bash
./mvnw -pl patient-service -am clean verify
```

Billing Service:

```bash
./mvnw -pl billing-service -am clean verify
```

`-am` builds any reactor modules required by the selected service, including shared contracts.

## Docker Compose

Start the local multi-service environment:

```bash
docker compose up --build
```

The current environment includes:

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

Inside Docker Compose, services communicate using Compose service names rather than `localhost`.

## Stop the Environment

Stop containers:

```bash
docker compose down
```

Remove containers and local persistent volumes:

```bash
docker compose down -v
```

> `docker compose down -v` permanently deletes data stored in the Compose-managed database volumes.

## Building Docker Images Directly

Docker builds use the repository root as their build context because services participate in the root Maven reactor and may depend on shared modules.

### Patient Service

```bash
docker build \
  --file patient-service/Dockerfile \
  --tag patient-management-system/patient-service:local \
  .
```

### Billing Service

```bash
docker build \
  --file billing-service/Dockerfile \
  --tag patient-management-system/billing-service:local \
  .
```

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

A typical workflow is:

```text
1. Create or switch to a feature branch
2. Make the application changes
3. Run targeted tests during development
4. Run ./mvnw clean verify
5. Build or run the affected services with Docker
6. Smoke-test the relevant service interaction
7. Commit and open a pull request
```

## Makefile

The repository Makefile provides shorter commands for common development tasks.

It is intended as a convenience layer over Maven and Docker rather than a replacement for them.

The Makefile may include targets for tasks such as:

```text
make setup
make build
make test
make up
make down
make clean
```

Run:

```bash
make help
```

for the authoritative list of available targets once the Makefile is present.

As additional services are added, the Makefile can expose service-specific targets without requiring developers to remember increasingly long Maven or Docker commands.
