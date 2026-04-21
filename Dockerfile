# STAGE 1: Build (The "Kitchen")
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom and fetch dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source and build the JAR
COPY src ./src
RUN mvn clean package -DskipTests

# STAGE 2: Run
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080

# Single line to avoid shell parsing errors
ENTRYPOINT ["sh", "-c", "java -Dspring.security.jwt.secret-key=${SPRING_SECURITY_JWT_SECRET_KEY} -Dapp.encryption.secret-key=${APP_ENCRYPTION_SECRET_KEY} -Dspring.datasource.url=${SPRING_DATASOURCE_URL} -Dspring.datasource.username=${SPRING_DATASOURCE_USERNAME} -Dspring.datasource.password=${SPRING_DATASOURCE_PASSWORD} -jar app.jar"]