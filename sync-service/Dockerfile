# Build stage
FROM maven:3.9.4-eclipse-temurin-21 AS build
WORKDIR /app

# Copiar tudo
COPY . .
COPY ../../pom.xml ./pom.xml

# Build completo
RUN mvn clean package -pl sync-service -am -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Install SQLite for database operations
RUN apk add --no-cache sqlite

# Copy the built jar
COPY --from=build /app/sync-service/target/*.jar app.jar

# Create data directory and set permissions
RUN mkdir -p /data && chmod 755 /data

# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring

# Expose port
EXPOSE 8082

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]