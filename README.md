# Distributed Inventory Management System

## Overview

This is an optimized distributed inventory management system designed for a chain of retail stores. The system addresses consistency and latency issues in inventory updates while ensuring security and observability across multiple stores.

## Architecture

The system follows a microservices architecture with the following components:

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Eureka Server │    │   API Gateway   │────│ Inventory Service│
│                 │    │                 │    │                 │
│ • Service Discovery│  │ • Authentication│    │ • CRUD Operations│
│ • Health Monitoring│  │ • Rate Limiting │    │ • Cache Management│
│ • Load Balancing │    │ • Circuit Breaker│    │ • Event Publishing│
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         └───────────────────────┼───────────────────────┘
                                 │
                    ┌─────────────────┐
                    │   Sync Service  │
                    │                 │
                    │ • Event Processing│
                    │ • Conflict Resolution│
                    │ • Scheduled Sync │
                    └─────────────────┘
                                 │
                    ┌─────────────────┐
                    │   Event Bus     │
                    │   (Kafka)       │
                    └─────────────────┘
                                 │
                    ┌─────────────────┐
                    │   Cache Layer  │
                    │   (Redis)       │
                    └─────────────────┘
```

## Technology Stack

- **Java 21** with Spring Boot 3.5.5
- **Spring Cloud Gateway** for API Gateway
- **Spring Cloud Netflix Eureka** for service discovery
- **Spring WebFlux** for reactive programming
- **Spring Data JPA** for data persistence
- **Redis** for distributed caching
- **Apache Kafka** for event-driven communication
- **SQLite** for local data storage (prototype)
- **Resilience4j** for circuit breaker patterns
- **Maven** for dependency management

## Key Features

### 1. **Optimized Consistency**
- Event-driven synchronization with Kafka
- Conflict resolution strategies
- Optimistic locking for concurrent updates
- Cache invalidation strategies

### 2. **Reduced Latency**
- Redis caching layer
- Asynchronous event processing
- Connection pooling
- Response compression

### 3. **Enhanced Security**
- JWT-based authentication
- Rate limiting per user/IP
- Input validation and sanitization
- Secure headers configuration

### 4. **Observability**
- Comprehensive logging with request tracing
- Health checks and metrics
- Circuit breaker monitoring
- Performance metrics collection

### 5. **Fault Tolerance**
- Circuit breaker patterns
- Retry mechanisms
- Graceful degradation
- Health check endpoints

## Services

### Eureka Server (Port 8761)
- **Service Discovery**: Central registry for all microservices
- **Health Monitoring**: Service health checks and monitoring
- **Load Balancing**: Client-side load balancing
- **Service Registration**: Automatic service registration and deregistration
- **Dashboard**: Web UI for service monitoring

### API Gateway (Port 8080)
- **Authentication**: JWT token validation
- **Rate Limiting**: Redis-based rate limiting
- **Circuit Breaker**: Resilience4j integration
- **Logging**: Request/response logging with tracing
- **Load Balancing**: Service discovery integration with Eureka

### Inventory Service (Port 8081)
- **CRUD Operations**: Full inventory management
- **Caching**: Redis-based caching with TTL
- **Event Publishing**: Kafka event publishing
- **Validation**: Input validation and business rules
- **Summary Reports**: Inventory analytics
- **Service Registration**: Registers with Eureka for discovery

### Sync Service (Port 8082)
- **Event Processing**: Kafka event consumption
- **Conflict Resolution**: Multiple resolution strategies
- **Scheduled Sync**: Periodic synchronization
- **Monitoring**: Sync status tracking
- **Service Registration**: Registers with Eureka for discovery

## API Endpoints

### Inventory Management

#### Get Inventory
```http
GET /api/inventory/{storeId}/{productId}
Authorization: Bearer <token>
```

#### Update Inventory
```http
PUT /api/inventory/{storeId}/{productId}
Content-Type: application/json
Authorization: Bearer <token>

{
  "quantity": 100
}
```

#### Decrement Inventory
```http
POST /api/inventory/{storeId}/{productId}/decrement?quantity=5
Authorization: Bearer <token>
```

#### Get Low Stock Items
```http
GET /api/inventory/{storeId}/low-stock?threshold=10
Authorization: Bearer <token>
```

#### Get Inventory Summary
```http
GET /api/inventory/{storeId}/summary
Authorization: Bearer <token>
```

### Sync Management

#### Trigger Manual Sync
```http
POST /api/sync/{storeId}
Authorization: Bearer <token>
```

#### Get Sync Status
```http
GET /api/sync/status/{syncId}
Authorization: Bearer <token>
```

## Configuration

### Environment Variables

```bash
# Database
SPRING_DATASOURCE_URL=jdbc:sqlite:inventory.db

# Redis
SPRING_DATA_REDIS_HOST=localhost
SPRING_DATA_REDIS_PORT=6379

# Kafka
SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# Security
JWT_ISSUER_URI=http://localhost:9000/auth/realms/inventory-system
```

### Application Properties

Each service has its own `application.yml` with service-specific configurations:

- **API Gateway**: Gateway routes, security, circuit breaker
- **Inventory Service**: Database, cache, Kafka producer
- **Sync Service**: Kafka consumer, scheduler, conflict resolution

## Setup Instructions

### Prerequisites
- Java 21+
- Maven 3.8+
- Docker and Docker Compose
- Redis (or Docker)
- Kafka (or Docker)

### 1. Clone Repository
```bash
git clone https://github.com/ErickNeivaFava/Distributed-Inventory-Management-System
cd Distributed-Inventory-Management-System
```

### 2. Start Infrastructure
```bash
docker-compose up -d
```

This starts:
- Redis on port 6379
- Zookeeper on port 2181
- Kafka on port 9092

### 3. Build and Run Services

#### Build All Services
```bash
mvn clean install
```

#### Run Eureka Server
```bash
cd eureka-service
mvn spring-boot:run -Dspring-boot.run.main-class=com.mercadolibre.sync.EurekaServerApplication
```

#### Run API Gateway
```bash
cd api-gateway
mvn spring-boot:run -Dspring-boot.run.main-class=com.mercadolibre.gateway.ApiGatewayApplication
```

#### Run Inventory Service
```bash
cd inventory-service
mvn spring-boot:run -Dspring-boot.run.main-class=com.mercadolibre.inventory.InventoryServiceApplication
```

#### Run Sync Service
```bash
cd sync-service
mvn spring-boot:run -Dspring-boot.run.main-class=com.mercadolibre.sync.SyncServiceApplication
```

### 4. Verify Services
- Eureka Server: http://localhost:8761 (admin/admin123)
- API Gateway: http://localhost:8080/actuator/health
- Inventory Service: http://localhost:8081/actuator/health
- Sync Service: http://localhost:8082/actuator/health

## Testing

### Unit Tests
```bash
mvn test
```

### Integration Tests
```bash
mvn verify
```

### Load Testing
Use tools like Apache JMeter or Artillery for load testing:

```bash
# Example Artillery configuration
artillery quick --count 100 --num 10 http://localhost:8080/api/inventory/store-1/product-1
```

## Monitoring and Observability

### Health Checks
- **API Gateway**: `/actuator/health`
- **Inventory Service**: `/actuator/health`
- **Sync Service**: `/actuator/health`

### Metrics
- **JVM Metrics**: Memory, GC, threads
- **Application Metrics**: Request count, response time
- **Custom Metrics**: Inventory operations, sync status

### Logging
- **Structured Logging**: JSON format for log aggregation
- **Request Tracing**: Unique request IDs
- **Correlation IDs**: Cross-service request tracking

## Performance Optimizations

### 1. **Caching Strategy**
- **L1 Cache**: Application-level caching with Spring Cache
- **L2 Cache**: Redis distributed cache
- **Cache Invalidation**: Event-driven cache invalidation

### 2. **Database Optimization**
- **Connection Pooling**: HikariCP configuration
- **Query Optimization**: Indexed queries and pagination
- **Batch Operations**: Bulk insert/update operations

### 3. **Event Processing**
- **Async Processing**: Non-blocking event handling
- **Batch Processing**: Bulk event processing
- **Dead Letter Queue**: Failed event handling

## Security Considerations

### 1. **Authentication**
- JWT token validation
- Token expiration handling
- Refresh token support

### 2. **Authorization**
- Role-based access control
- Resource-level permissions
- API key management

### 3. **Data Protection**
- Input validation and sanitization
- SQL injection prevention
- XSS protection

## Deployment

### Docker Deployment
```bash
# Build images
docker build -t inventory-api-gateway ./api-gateway
docker build -t inventory-service ./inventory-service
docker build -t inventory-sync-service ./sync-service
docker build -t eureka-server ./eureka-server

# Run with docker-compose
docker-compose -f docker-compose.prod.yml up -d
```

### Kubernetes Deployment
```bash
# Apply configurations
kubectl apply -f k8s/
```

## Troubleshooting

### Common Issues

#### 1. **Service Discovery Issues**
- Check Eureka server connectivity
- Verify service registration
- Check network connectivity

#### 2. **Cache Issues**
- Verify Redis connectivity
- Check cache configuration
- Monitor cache hit rates

#### 3. **Event Processing Issues**
- Check Kafka connectivity
- Verify topic configuration
- Monitor consumer lag

### Debug Mode
Enable debug logging:
```yaml
logging:
  level:
    com.mercadolibre: DEBUG
    org.springframework.cloud.gateway: DEBUG
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For support and questions:
- Create an issue in the repository
- Contact the development team
- Check the documentation wiki
