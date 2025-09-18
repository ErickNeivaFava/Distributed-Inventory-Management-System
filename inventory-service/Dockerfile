# Build stage
FROM maven:3.9.4-eclipse-temurin-21 AS build
WORKDIR /app

# Copy the entire project structure
COPY . .
COPY ../../pom.xml ./pom.xml

# Primeiro instala o POM pai no repositório local
#RUN mvn install -N -DskipTests

# Agora baixa as dependências do módulo específico
#RUN mvn dependency:go-offline -pl inventory-service

# Build da aplicação
RUN mvn clean package -pl inventory-service -am -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Install SQLite for database operations
RUN apk add --no-cache sqlite

# Copy the built jar
COPY --from=build /app/inventory-service/target/*.jar app.jar

# Create data directory and set permissions
RUN mkdir -p /app/data && chmod 777 /app/data

# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring

# Expose port
EXPOSE 8081

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]