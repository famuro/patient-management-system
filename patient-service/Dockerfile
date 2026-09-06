# ============================================================
# Build stage
# ============================================================
# Use an official Maven image with Java 21 to compile and package
# the Spring Boot application in a reproducible build environment.
FROM maven:3.9.16-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Copy the Maven project descriptor first so dependency resolution can
# be cached independently from application source changes.
COPY pom.xml .

# Download application dependencies before copying source code.
# This improves Docker layer reuse when only source files change.
RUN mvn --batch-mode --no-transfer-progress dependency:go-offline

# Copy application source and build the executable Spring Boot JAR.
COPY src ./src

# Tests are executed separately by the CI pipeline before the image build,
# so they are skipped here to avoid running the same suite twice.
RUN mvn --batch-mode --no-transfer-progress clean package -DskipTests


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
COPY --from=builder /app/target/patient-service.jar app.jar

# Ensure the application process does not run as root.
USER spring:spring

# Document the port used by the Patient Service.
EXPOSE 4000

# Run the Spring Boot application as the container's main process.
ENTRYPOINT ["java", "-jar", "app.jar"]
