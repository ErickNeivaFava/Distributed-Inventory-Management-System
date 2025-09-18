FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Instalar Maven e outras dependências
RUN apk update && apk add --no-cache maven curl

# Copiar TODO o projeto (incluindo o POM pai)
COPY . .
COPY ../../pom.xml ./pom.xml

# Primeiro instalar o POM pai no repositório local
#RUN mvn install -N -DskipTests

# Baixar dependências do eureka-server
#RUN mvn dependency:go-offline -pl eureka-server

# Build da aplicação
RUN mvn clean package -pl eureka-server -am -DskipTests

# Criar usuário não-root
RUN addgroup -S appuser && adduser -S appuser -G appuser
RUN chown -R appuser:appuser /app
USER appuser

# Expor porta
EXPOSE 8761

# Health check (usando wget que é mais comum no Alpine)
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost:8761/actuator/health || exit 1

# Executar aplicação
CMD ["java", "-jar", "eureka-server/target/eureka-server-0.0.1-SNAPSHOT.jar"]