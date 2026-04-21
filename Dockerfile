# STAGE 1: Build (The "Kitchen")
# Make sure "AS build" is exactly like this
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom and fetch dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source and build the JAR
COPY src ./src
RUN mvn clean package -DskipTests

# STAGE 2: Run (The "Service")
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copy the built JAR from the first stage
COPY --from=build /app/target/*.jar app.jar

# IMPORTANT: No ENV lines here!
# Render will inject them automatically at runtime.

EXPOSE 8080

# Shell form ENTRYPOINT to ensure Env Vars are visible
ENTRYPOINT java -jar app.jar