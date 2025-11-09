# Multi-stage Dockerfile for Spring Boot app

# 1) Build stage
FROM maven:3.9.9-eclipse-temurin-17-alpine AS builder
WORKDIR /app

# Copy Maven descriptor and source
COPY pom.xml ./
COPY src ./src

# Build the application (skip tests for faster build)
RUN mvn -B -DskipTests clean package

# 2) Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring \
    && mkdir -p /app && chown -R spring:spring /app
USER spring:spring

# Copy the fat jar from the builder stage
COPY --from=builder /app/target/*.jar /app/app.jar

# Expose the default Spring Boot port
EXPOSE 8080

# Allow passing JVM args via JAVA_OPTS
ENV JAVA_OPTS=""

# Start the app
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]

