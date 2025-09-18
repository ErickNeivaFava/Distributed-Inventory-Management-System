# Eureka Server Configuration Guide

## Overview

O Eureka Server é o componente central de service discovery no sistema de inventário distribuído. Ele permite que os microserviços se registrem automaticamente e descubram uns aos outros sem configuração manual de URLs.

## Configuração do Eureka Server

### 1. Estrutura do Projeto

```
eureka-server/
├── pom.xml
├── Dockerfile
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── mercadolibre/
        │           └── eureka/
        │               ├── EurekaServerApplication.java
        │               └── config/
        │                   └── SecurityConfig.java
        └── resources/
            ├── application.yml
            └── application-docker.yml
```

### 2. Configuração Principal (application.yml)

```yaml
server:
  port: 8761

spring:
  application:
    name: eureka-server
  security:
    user:
      name: admin
      password: admin123

eureka:
  instance:
    hostname: localhost
  client:
    register-with-eureka: false  # Eureka Server não se registra consigo mesmo
    fetch-registry: false        # Eureka Server não busca outros servidores
    service-url:
      defaultZone: http://admin:admin123@localhost:8761/eureka/
  server:
    enable-self-preservation: false  # Desabilita auto-preservação para desenvolvimento
    eviction-interval-timer-in-ms: 5000
    response-cache-update-interval-ms: 5000
```

### 3. Configuração Docker (application-docker.yml)

```yaml
server:
  port: 8761

spring:
  application:
    name: eureka-server
  security:
    user:
      name: admin
      password: admin123

eureka:
  instance:
    hostname: eureka-server
    prefer-ip-address: true
  client:
    register-with-eureka: false
    fetch-registry: false
    service-url:
      defaultZone: http://admin:admin123@eureka-server:8761/eureka/
  server:
    enable-self-preservation: false
    eviction-interval-timer-in-ms: 5000
    response-cache-update-interval-ms: 5000
```

## Configuração dos Clientes Eureka

### 1. API Gateway

```yaml
eureka:
  client:
    service-url:
      defaultZone: http://admin:admin123@localhost:8761/eureka/
    register-with-eureka: true
    fetch-registry: true
  instance:
    prefer-ip-address: true
    lease-renewal-interval-in-seconds: 30
    lease-expiration-duration-in-seconds: 90
```

### 2. Inventory Service

```yaml
eureka:
  client:
    service-url:
      defaultZone: http://admin:admin123@localhost:8761/eureka/
    register-with-eureka: true
    fetch-registry: true
  instance:
    prefer-ip-address: true
    lease-renewal-interval-in-seconds: 30
    lease-expiration-duration-in-seconds: 90
```

### 3. Sync Service

```yaml
eureka:
  client:
    service-url:
      defaultZone: http://admin:admin123@localhost:8761/eureka/
    register-with-eureka: true
    fetch-registry: true
  instance:
    prefer-ip-address: true
    lease-renewal-interval-in-seconds: 30
    lease-expiration-duration-in-seconds: 90
```

## Configuração de Segurança

### SecurityConfig.java

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/actuator/**").permitAll()
                .anyRequest().authenticated()
            )
            .httpBasic();
        
        return http.build();
    }
}
```

## Dockerfile

```dockerfile
FROM openjdk:21-jdk-slim

WORKDIR /app

# Copy Maven files
COPY eureka-server/pom.xml .
COPY pom.xml ../

# Download dependencies
RUN apt-get update && apt-get install -y maven && \
    mvn dependency:go-offline -B && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

# Copy source code
COPY eureka-server/src ./src

# Build application
RUN mvn clean package -DskipTests

# Create non-root user
RUN groupadd -r appuser && useradd -r -g appuser appuser
RUN chown -R appuser:appuser /app
USER appuser

# Expose port
EXPOSE 8761

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8761/actuator/health || exit 1

# Run application
CMD ["java", "-jar", "target/eureka-server-0.0.1-SNAPSHOT.jar"]
```

## Docker Compose

```yaml
eureka-server:
  build:
    context: .
    dockerfile: eureka-server/Dockerfile
  container_name: eureka-server
  ports:
    - "8761:8761"
  networks:
    - inventory-network
  environment:
    - SPRING_PROFILES_ACTIVE=docker
  restart: unless-stopped
```

## Funcionalidades do Eureka

### 1. Service Discovery
- **Registro Automático**: Serviços se registram automaticamente no startup
- **Descoberta Dinâmica**: Clientes descobrem serviços disponíveis
- **Health Checks**: Monitoramento contínuo da saúde dos serviços

### 2. Load Balancing
- **Client-Side**: Balanceamento de carga no lado do cliente
- **Round Robin**: Distribuição uniforme de requisições
- **Failover**: Redirecionamento automático em caso de falha

### 3. Monitoring
- **Dashboard Web**: Interface web para monitoramento
- **Métricas**: Coleta de métricas de serviços
- **Logs**: Logs detalhados de operações

## Acessando o Dashboard

### URL: http://localhost:8761
- **Usuário**: admin
- **Senha**: admin123

### Informações Disponíveis
- Lista de serviços registrados
- Status de saúde de cada serviço
- Instâncias ativas
- Métricas de performance

## Troubleshooting

### Problemas Comuns

#### 1. Serviço não se registra
```bash
# Verificar logs do serviço
docker logs inventory-service

# Verificar conectividade com Eureka
curl http://localhost:8761/eureka/apps
```

#### 2. Eureka Server não inicia
```bash
# Verificar logs do Eureka Server
docker logs eureka-server

# Verificar se a porta está disponível
netstat -tulpn | grep 8761
```

#### 3. Serviços não são descobertos
```bash
# Verificar registro no Eureka
curl -u admin:admin123 http://localhost:8761/eureka/apps/inventory-service

# Verificar configuração do cliente
grep -r "eureka" inventory-service/src/main/resources/
```

### Logs Úteis

#### Eureka Server
```bash
# Logs de registro de serviços
grep "Registered instance" eureka-server.log

# Logs de health checks
grep "Renew" eureka-server.log
```

#### Cliente Eureka
```bash
# Logs de registro
grep "Registering application" inventory-service.log

# Logs de descoberta
grep "DiscoveryClient" inventory-service.log
```

## Configurações Avançadas

### 1. Clustering (Produção)
```yaml
eureka:
  server:
    peer-eureka-nodes-update-interval-ms: 10000
    peer-eureka-status-refresh-time-interval-ms: 10000
  client:
    service-url:
      defaultZone: http://eureka1:8761/eureka/,http://eureka2:8761/eureka/
```

### 2. Configurações de Performance
```yaml
eureka:
  server:
    response-cache-update-interval-ms: 30000
    response-cache-auto-expiration-in-seconds: 180
  instance:
    lease-renewal-interval-in-seconds: 30
    lease-expiration-duration-in-seconds: 90
```

### 3. Configurações de Segurança
```yaml
eureka:
  server:
    enable-self-preservation: true
    renewal-percent-threshold: 0.85
  client:
    eureka-service-url-poll-interval-seconds: 300
```

## Monitoramento

### Health Checks
```bash
# Health check do Eureka Server
curl http://localhost:8761/actuator/health

# Status dos serviços registrados
curl -u admin:admin123 http://localhost:8761/eureka/apps
```

### Métricas
```bash
# Métricas do Eureka Server
curl http://localhost:8761/actuator/metrics

# Métricas específicas
curl http://localhost:8761/actuator/metrics/eureka.server.registry.size
```

## Boas Práticas

### 1. Configuração de Rede
- Use IP addresses em vez de hostnames em ambientes containerizados
- Configure `prefer-ip-address: true` para melhor compatibilidade

### 2. Health Checks
- Implemente health checks robustos nos serviços
- Configure timeouts apropriados para health checks

### 3. Logging
- Configure logging apropriado para troubleshooting
- Use structured logging para melhor análise

### 4. Segurança
- Use autenticação em ambientes de produção
- Configure HTTPS para comunicação segura

## Referências

- [Spring Cloud Netflix Eureka Documentation](https://spring.io/projects/spring-cloud-netflix)
- [Eureka Server Configuration](https://docs.spring.io/spring-cloud-netflix/docs/current/reference/html/#eureka-server)
- [Eureka Client Configuration](https://docs.spring.io/spring-cloud-netflix/docs/current/reference/html/#eureka-client)
