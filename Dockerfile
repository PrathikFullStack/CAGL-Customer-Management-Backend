# Multi-service flexible Dockerfile for CAGL-Customer-Management-Backend
# Build argument to specify which microservice to build (e.g. AppzillonBanking-CM, AppzillonBanking-CAGL)
ARG SERVICE_NAME=AppzillonBanking-CM

# Stage 1: Build the shared dependencies and selected microservice
FROM maven:3.9-eclipse-temurin-17 AS builder
ARG SERVICE_NAME

WORKDIR /workspace

# Copy shared dependency modules and install into local Maven repo
COPY dependencies-lib ./dependencies-lib
RUN mvn clean install -DskipTests -f dependencies-lib/pom.xml

# Copy the microservice source code
COPY apz_java_microservices/${SERVICE_NAME} ./service

# Package the selected microservice
WORKDIR /workspace/service
RUN mvn clean package -DskipTests

# Stage 2: Create lightweight runtime container
FROM eclipse-temurin:17-jre-alpine
ARG SERVICE_NAME

WORKDIR /app

# Add non-root system user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser:appgroup

# Copy built JAR artifact from builder stage
COPY --from=builder /workspace/service/target/*.jar /app/app.jar

# JVM Performance and memory limit tuning
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT} -jar /app/app.jar"]
