# Build stage
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copy Maven wrapper and pom first
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw

# Download dependencies
RUN ./mvnw dependency:go-offline

# Copy source and list files for debugging
COPY src ./src
RUN echo "--- VERIFYING SOURCE FILES ---" && find src -name "*.java"

# Build the application
RUN ./mvnw clean package -DskipTests

# Verify built classes
RUN echo "--- VERIFYING BUILT CLASSES ---" && find target/classes -name "*.class"

# Run stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/backend-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
