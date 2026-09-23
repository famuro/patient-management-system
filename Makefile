# ============================================================
# Patient Management System - Development Commands
# ============================================================
#
# This Makefile provides convenient wrappers around Maven and Docker
# commands used during local development. Maven and Docker Compose
# remain the underlying build and orchestration tools.
#
# Run `make help` to see the available commands.
# ============================================================

SHELL := /bin/sh

.DEFAULT_GOAL := help

# ------------------------------------------------------------
# Tooling
# ------------------------------------------------------------

MAVEN ?= ./mvnw
COMPOSE ?= docker compose

# ------------------------------------------------------------
# Environment
# ------------------------------------------------------------

ENV_FILE ?= .env
ENV_EXAMPLE ?= .env.example

# ------------------------------------------------------------
# Maven modules
# ------------------------------------------------------------

PATIENT_MODULE ?= patient-service
BILLING_MODULE ?= billing-service

# ------------------------------------------------------------
# Docker images
# ------------------------------------------------------------

PATIENT_IMAGE ?= patient-management-system/patient-service:local
BILLING_IMAGE ?= patient-management-system/billing-service:local


.PHONY: \
	help \
	setup \
	env \
	test \
	build \
	package \
	clean \
	test-patient \
	test-billing \
	verify-patient \
	verify-billing \
	up \
	up-build \
	down \
	down-volumes \
	restart \
	logs \
	ps \
	docker-build \
	docker-build-patient \
	docker-build-billing


# ============================================================
# Help
# ============================================================

help: ## Show available Makefile commands
	@awk 'BEGIN { \
		FS = ":.*## "; \
		printf "\nUsage:\n  make <target>\n\nTargets:\n"; \
	} \
	/^[a-zA-Z0-9_-]+:.*## / { \
		printf "  %-22s %s\n", $$1, $$2; \
	}' $(MAKEFILE_LIST)
	@printf "\n"

# ============================================================
# Environment setup
# ============================================================

setup: env ## Prepare the repository for local development
	@chmod +x mvnw
	@echo "Development environment is ready."

env: ## Create .env from .env.example if it does not already exist
	@if [ -f "$(ENV_FILE)" ]; then \
		echo "$(ENV_FILE) already exists; leaving it unchanged."; \
	elif [ ! -f "$(ENV_EXAMPLE)" ]; then \
		echo "Error: $(ENV_EXAMPLE) was not found."; \
		exit 1; \
	else \
		cp "$(ENV_EXAMPLE)" "$(ENV_FILE)"; \
		echo "Created $(ENV_FILE) from $(ENV_EXAMPLE)."; \
	fi

# ============================================================
# Maven
# ============================================================

test: ## Run tests for all Maven modules
	$(MAVEN) --batch-mode --no-transfer-progress test

build: ## Clean, test, and verify the complete Maven project
	$(MAVEN) --batch-mode --no-transfer-progress clean verify

package: ## Package all modules without running tests
	$(MAVEN) --batch-mode --no-transfer-progress clean package -DskipTests

clean: ## Remove Maven build output
	$(MAVEN) --batch-mode --no-transfer-progress clean

test-patient: ## Run Patient Service tests and required modules
	$(MAVEN) --batch-mode --no-transfer-progress \
		-pl $(PATIENT_MODULE) \
		-am \
		test


test-billing: ## Run Billing Service tests and required modules
	$(MAVEN) --batch-mode --no-transfer-progress \
		-pl $(BILLING_MODULE) \
		-am \
		test


verify-patient: ## Clean and verify the Patient Service and required modules
	$(MAVEN) --batch-mode --no-transfer-progress \
		-pl $(PATIENT_MODULE) \
		-am \
		clean verify


verify-billing: ## Clean and verify the Billing Service and required modules
	$(MAVEN) --batch-mode --no-transfer-progress \
		-pl $(BILLING_MODULE) \
		-am \
		clean verify


# ============================================================
# Docker Compose
# ============================================================

up: env ## Start the local application stack
	$(COMPOSE) up -d

up-build: env ## Build images and start the local application stack
	$(COMPOSE) up --build -d

down: ## Stop and remove local application containers
	$(COMPOSE) down

down-volumes: ## Stop containers and permanently remove Compose volumes
	$(COMPOSE) down -v

restart: down up ## Restart the local application stack

logs: ## Follow logs from all Compose services
	$(COMPOSE) logs -f

ps: ## Show the status of Compose services
	$(COMPOSE) ps

# ============================================================
# Docker image builds
# ============================================================

docker-build: docker-build-patient docker-build-billing ## Build all service images

docker-build-patient: ## Build the Patient Service Docker image
	docker build \
		--file patient-service/Dockerfile \
		--tag $(PATIENT_IMAGE) \
		.

docker-build-billing: ## Build the Billing Service Docker image
	docker build \
		--file billing-service/Dockerfile \
		--tag $(BILLING_IMAGE) \
		.
