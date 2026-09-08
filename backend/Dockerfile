# =========================================================================
# STATE SMART LIFE - BACKEND DOCKER CONTAINER CONFIGURATION
# =========================================================================

# Build Stage
FROM maven:3.8.5-openjdk-17-slim AS build
WORKDIR /app

# Copy the pom.xml and source code
COPY pom.xml .
COPY src ./src

# Compile and package the application
RUN mvn clean package -DskipTests

# Runtime Stage
FROM openjdk:17-jdk-slim
WORKDIR /app

# Copy the build artifact from build stage
COPY --from=build /app/target/state-smart-life-0.0.1.jar app.jar

# Expose API port
EXPOSE 8080

# Configure Spring Profile and run application
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]
