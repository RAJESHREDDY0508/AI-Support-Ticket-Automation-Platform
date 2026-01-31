# Build stage - uses backend root for multi-module build
FROM gradle:8.5-jdk17-alpine AS builder

WORKDIR /app

# Copy root build files (settings, root build, gradle properties)
COPY settings.gradle.kts build.gradle.kts gradle.properties ./
COPY gradle gradle

# Copy subprojects
COPY api-contracts api-contracts
COPY services/ticket-service services/ticket-service

# Build ticket-service (depends on api-contracts)
RUN gradle :services:ticket-service:bootJar --no-daemon -x test

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

RUN addgroup -g 1000 appgroup && adduser -u 1000 -G appgroup -D appuser
USER appuser

COPY --from=builder /app/services/ticket-service/build/libs/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
