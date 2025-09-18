FROM maven:3.9.4-eclipse-temurin-21 AS build
WORKDIR /app

# Copiar tudo
COPY . .
COPY ../../pom.xml ./pom.xml

# Build apenas do api-gateway e suas dependências
RUN mvn clean package -pl api-gateway -am -DskipTests

# Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Instalar curl para healthcheck
RUN apk update && apk add --no-cache curl

# Create data directory and set permissions
RUN mkdir -p /app/data && chmod 777 /app/data

COPY --from=build /app/api-gateway/target/api-gateway-0.0.1-SNAPSHOT.jar app.jar

# Criar usuário não-root
RUN addgroup -S spring && adduser -S spring -G spring
USER spring

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

CMD ["java", "-jar", "app.jar"]