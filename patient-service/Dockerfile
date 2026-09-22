# ============================================================
# Build stage
# ============================================================
# Use an official Maven image with Java 21 to compile and package
# the Spring Boot application in a reproducible build environment.
FROM maven:3.9.16-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy Maven descriptors first so dependency resolution can be cached
# independently from application source changes.
COPY pom.xml .
COPY patient-service/pom.xml patient-service/pom.xml
COPY billing-service/pom.xml billing-service/pom.xml
COPY contracts/billing-contract/pom.xml contracts/billing-contract/pom.xml

# Download dependencies for the Patient Service and any reactor modules
# required by its dependency graph.
RUN mvn --batch-mode --no-transfer-progress \
    -pl patient-service \
    -am \
    dependency:go-offline

# Copy the shared gRPC contract source and the Patient Service source.
# Patient depends on the generated Billing contract classes, so the
# protobuf contract must be available during the reactor build.
COPY contracts/billing-contract/src contracts/billing-contract/src
COPY patient-service/src patient-service/src

# Build the Patient Service from the root Maven reactor.
# Tests are executed separately by CI before the Docker image is built.
RUN mvn --batch-mode --no-transfer-progress \
    -pl patient-service \
    -am \
    clean package \
    -DskipTests


# ============================================================
# Runtime stage
# ============================================================
# Use a JRE-only image for the final container because compilation tools
# are not required at runtime.
FROM eclipse-temurin:21-jre-noble

WORKDIR /app

# Create a dedicated non-root user for the application.
# Running as non-root reduces the impact of a container compromise.
RUN groupadd --system spring \
    && useradd --system --gid spring spring

# Copy only the packaged application artifact from the build stage.
# Maven and the full JDK are not included in the final runtime image.
COPY --from=builder \
    /app/patient-service/target/patient-service.jar \
    app.jar

# Ensure the application process does not run as root.
USER spring:spring

# Document the port used by the Patient Service.
EXPOSE 4000

# Run the Spring Boot application as the container's main process.
ENTRYPOINT ["java", "-jar", "app.jar"]