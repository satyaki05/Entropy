# Stage 1: Build the JAR using Maven
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
# This caches your dependencies so builds are faster later
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Create the final lightweight image
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
# Copy only the built JAR from the first stage
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "echo 'DEBUG: JWT_KEY_LENGTH='${#SPRING_SECURITY_JWT_SECRET_KEY} && java -jar app.jar"]