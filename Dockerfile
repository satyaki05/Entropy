# STAGE 1: Build (The "Kitchen")
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

EXPOSE 8080

# This is the "Final Boss" command. It bypasses all property files
# and injects the Render secrets directly into the Java process.
ENTRYPOINT ["sh", "-c", "java \
    -Dspring.security.jwt.secret-key=${SPRING_SECURITY_JWT_SECRET_KEY} \
    -Dapp.encryption.secret-key=${APP_ENCRYPTION_SECRET_KEY} \
    -Dspring.datasource.url=${SPRING_DATASOURCE_URL} \
    -Dspring.datasource.username=${SPRING_DATASOURCE_USERNAME} \
    -Dspring.datasource.password=${SPRING_DATASOURCE_PASSWORD} \
    -jar app.jar"]