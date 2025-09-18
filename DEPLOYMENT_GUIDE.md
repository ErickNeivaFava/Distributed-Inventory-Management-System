# Deployment Guide

## Overview

This guide provides comprehensive instructions for deploying the Distributed Inventory Management System in various environments.

## Prerequisites

### System Requirements
- **CPU**: 2+ cores
- **Memory**: 4GB+ RAM
- **Storage**: 10GB+ available space
- **Network**: Internet connectivity for package downloads

### Software Requirements
- **Java**: 21 or higher
- **Maven**: 3.8 or higher
- **Docker**: 20.10 or higher
- **Docker Compose**: 2.0 or higher
- **Kubernetes**: 1.20+ (for K8s deployment)

## Environment Configurations

### Development Environment

#### 1. Local Development Setup

**Step 1: Clone Repository**
```bash
git clone <repository-url>
cd inventory-system-v2
```

**Step 2: Start Infrastructure**
```bash
docker-compose up -d
```

**Step 3: Build Services**
```bash
mvn clean install
```

**Step 4: Run Services**
```bash
# Terminal 1 - API Gateway
cd api-gateway && mvn spring-boot:run

# Terminal 2 - Inventory Service
cd inventory-service && mvn spring-boot:run

# Terminal 3 - Sync Service
cd sync-service && mvn spring-boot:run
```

**Step 5: Verify Deployment**
```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
```

### Staging Environment

#### 1. Docker Compose Deployment

**Create staging configuration:**
```yaml
# docker-compose.staging.yml
version: '3.8'
services:
  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    environment:
      - REDIS_PASSWORD=staging_password
    volumes:
      - redis_data:/data

  kafka:
    image: confluentinc/cp-kafka:7.3.0
    ports:
      - "9092:9092"
    environment:
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
    depends_on:
      - zookeeper

  zookeeper:
    image: confluentinc/cp-zookeeper:7.3.0
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181

  api-gateway:
    build: ./api-gateway
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=staging
      - SPRING_DATA_REDIS_HOST=redis
      - SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092
    depends_on:
      - redis
      - kafka

  inventory-service:
    build: ./inventory-service
    ports:
      - "8081:8081"
    environment:
      - SPRING_PROFILES_ACTIVE=staging
      - SPRING_DATA_REDIS_HOST=redis
      - SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092
    depends_on:
      - redis
      - kafka

  sync-service:
    build: ./sync-service
    ports:
      - "8082:8082"
    environment:
      - SPRING_PROFILES_ACTIVE=staging
      - SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092
    depends_on:
      - kafka

volumes:
  redis_data:
```

**Deploy to staging:**
```bash
docker-compose -f docker-compose.staging.yml up -d
```

#### 2. Environment-Specific Configuration

**Create staging application.yml:**
```yaml
# api-gateway/src/main/resources/application-staging.yml
server:
  port: 8080

spring:
  application:
    name: api-gateway
  profiles:
    active: staging
  redis:
    host: redis
    port: 6379
    password: staging_password
  kafka:
    bootstrap-servers: kafka:9092

logging:
  level:
    com.mercadolibre: INFO
    org.springframework.cloud.gateway: INFO
```

### Production Environment

#### 1. Kubernetes Deployment

**Create namespace:**
```bash
kubectl create namespace inventory-system
```

**Create ConfigMap:**
```yaml
# k8s/configmap.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: inventory-config
  namespace: inventory-system
data:
  application.yml: |
    spring:
      profiles:
        active: production
      redis:
        host: redis-service
        port: 6379
      kafka:
        bootstrap-servers: kafka-service:9092
    logging:
      level:
        com.mercadolibre: WARN
```

**Create Redis deployment:**
```yaml
# k8s/redis.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: redis
  namespace: inventory-system
spec:
  replicas: 1
  selector:
    matchLabels:
      app: redis
  template:
    metadata:
      labels:
        app: redis
    spec:
      containers:
      - name: redis
        image: redis:7-alpine
        ports:
        - containerPort: 6379
        resources:
          requests:
            memory: "256Mi"
            cpu: "250m"
          limits:
            memory: "512Mi"
            cpu: "500m"
---
apiVersion: v1
kind: Service
metadata:
  name: redis-service
  namespace: inventory-system
spec:
  selector:
    app: redis
  ports:
  - port: 6379
    targetPort: 6379
```

**Create Kafka deployment:**
```yaml
# k8s/kafka.yaml
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: kafka
  namespace: inventory-system
spec:
  serviceName: kafka-service
  replicas: 1
  selector:
    matchLabels:
      app: kafka
  template:
    metadata:
      labels:
        app: kafka
    spec:
      containers:
      - name: kafka
        image: confluentinc/cp-kafka:7.3.0
        ports:
        - containerPort: 9092
        env:
        - name: KAFKA_ZOOKEEPER_CONNECT
          value: "zookeeper-service:2181"
        - name: KAFKA_ADVERTISED_LISTENERS
          value: "PLAINTEXT://kafka-service:9092"
        - name: KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR
          value: "1"
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
---
apiVersion: v1
kind: Service
metadata:
  name: kafka-service
  namespace: inventory-system
spec:
  selector:
    app: kafka
  ports:
  - port: 9092
    targetPort: 9092
```

**Create API Gateway deployment:**
```yaml
# k8s/api-gateway.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: api-gateway
  namespace: inventory-system
spec:
  replicas: 3
  selector:
    matchLabels:
      app: api-gateway
  template:
    metadata:
      labels:
        app: api-gateway
    spec:
      containers:
      - name: api-gateway
        image: inventory-api-gateway:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "production"
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
---
apiVersion: v1
kind: Service
metadata:
  name: api-gateway-service
  namespace: inventory-system
spec:
  selector:
    app: api-gateway
  ports:
  - port: 80
    targetPort: 8080
  type: LoadBalancer
```

**Deploy to Kubernetes:**
```bash
kubectl apply -f k8s/
```

#### 2. Production Configuration

**Environment variables:**
```bash
# Production environment variables
export SPRING_PROFILES_ACTIVE=production
export SPRING_DATASOURCE_URL=jdbc:postgresql://db-host:5432/inventory_prod
export SPRING_DATA_REDIS_HOST=redis-host
export SPRING_DATA_REDIS_PASSWORD=secure_password
export SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka-host:9092
export JWT_ISSUER_URI=https://auth.company.com/auth/realms/inventory-system
export MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info,metrics
```

**Production application.yml:**
```yaml
# application-production.yml
server:
  port: 8080
  compression:
    enabled: true
  http2:
    enabled: true

spring:
  application:
    name: inventory-system
  profiles:
    active: production
  datasource:
    url: ${SPRING_DATASOURCE_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
  redis:
    host: ${SPRING_DATA_REDIS_HOST}
    port: 6379
    password: ${SPRING_DATA_REDIS_PASSWORD}
    timeout: 2000
    lettuce:
      pool:
        max-active: 20
        max-idle: 10
  kafka:
    bootstrap-servers: ${SPRING_KAFKA_BOOTSTRAP_SERVERS}
    producer:
      acks: all
      retries: 3
      batch-size: 16384
      linger-ms: 5
    consumer:
      group-id: inventory-system
      auto-offset-reset: earliest
      enable-auto-commit: false

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: when-authorized
  metrics:
    export:
      prometheus:
        enabled: true

logging:
  level:
    com.mercadolibre: WARN
    org.springframework: WARN
    org.hibernate: WARN
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: /var/log/inventory-system/application.log
```

## Database Setup

### PostgreSQL (Production)

**Create database:**
```sql
CREATE DATABASE inventory_prod;
CREATE USER inventory_user WITH PASSWORD 'secure_password';
GRANT ALL PRIVILEGES ON DATABASE inventory_prod TO inventory_user;
```

**Run migrations:**
```bash
mvn flyway:migrate -Dflyway.url=jdbc:postgresql://db-host:5432/inventory_prod
```

### SQLite (Development)

**Automatic setup:**
```bash
# Database is created automatically on first run
mvn spring-boot:run
```

## Monitoring Setup

### 1. Prometheus Configuration

**prometheus.yml:**
```yaml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'inventory-system'
    static_configs:
      - targets: ['api-gateway:8080', 'inventory-service:8081', 'sync-service:8082']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 5s
```

### 2. Grafana Dashboard

**Import dashboard configuration:**
```json
{
  "dashboard": {
    "title": "Inventory System Metrics",
    "panels": [
      {
        "title": "Request Rate",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(http_requests_total[5m])",
            "legendFormat": "{{instance}}"
          }
        ]
      },
      {
        "title": "Response Time",
        "type": "graph",
        "targets": [
          {
            "expr": "histogram_quantile(0.95, rate(http_request_duration_seconds_bucket[5m]))",
            "legendFormat": "95th percentile"
          }
        ]
      }
    ]
  }
}
```

### 3. Log Aggregation

**ELK Stack configuration:**
```yaml
# docker-compose.logging.yml
version: '3.8'
services:
  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.5.0
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=false
    ports:
      - "9200:9200"

  logstash:
    image: docker.elastic.co/logstash/logstash:8.5.0
    volumes:
      - ./logstash.conf:/usr/share/logstash/pipeline/logstash.conf
    ports:
      - "5044:5044"

  kibana:
    image: docker.elastic.co/kibana/kibana:8.5.0
    ports:
      - "5601:5601"
```

## Security Configuration

### 1. SSL/TLS Setup

**Generate certificates:**
```bash
# Generate self-signed certificate for development
keytool -genkeypair -alias inventory-system -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore keystore.p12 -validity 3650

# For production, use certificates from a trusted CA
```

**Configure SSL:**
```yaml
server:
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: changeit
    key-store-type: PKCS12
    key-alias: inventory-system
```

### 2. Network Security

**Firewall rules:**
```bash
# Allow only necessary ports
ufw allow 80/tcp   # HTTP
ufw allow 443/tcp  # HTTPS
ufw allow 22/tcp   # SSH
ufw deny 8080/tcp  # Block direct access to services
ufw deny 8081/tcp
ufw deny 8082/tcp
```

### 3. Authentication Setup

**JWT Configuration:**
```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: ${JWT_ISSUER_URI}
          jwk-set-uri: ${JWT_ISSUER_URI}/protocol/openid-connect/certs
```

## Backup and Recovery

### 1. Database Backup

**Automated backup script:**
```bash
#!/bin/bash
# backup.sh
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/backups"
DB_NAME="inventory_prod"

# Create backup
pg_dump -h $DB_HOST -U $DB_USER $DB_NAME > $BACKUP_DIR/inventory_backup_$DATE.sql

# Compress backup
gzip $BACKUP_DIR/inventory_backup_$DATE.sql

# Remove old backups (keep last 7 days)
find $BACKUP_DIR -name "inventory_backup_*.sql.gz" -mtime +7 -delete
```

**Schedule backup:**
```bash
# Add to crontab
0 2 * * * /path/to/backup.sh
```

### 2. Application Data Backup

**Backup Redis data:**
```bash
# Create Redis backup
redis-cli --rdb /backups/redis_backup_$(date +%Y%m%d_%H%M%S).rdb
```

**Backup Kafka topics:**
```bash
# Export Kafka topics
kafka-console-consumer --bootstrap-server localhost:9092 --topic inventory-events --from-beginning > /backups/kafka_backup_$(date +%Y%m%d_%H%M%S).json
```

## Performance Tuning

### 1. JVM Tuning

**Production JVM options:**
```bash
JAVA_OPTS="-Xms2g -Xmx4g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UseStringDeduplication -XX:+OptimizeStringConcat"
```

### 2. Database Tuning

**PostgreSQL configuration:**
```sql
-- Increase shared_buffers
ALTER SYSTEM SET shared_buffers = '256MB';

-- Increase effective_cache_size
ALTER SYSTEM SET effective_cache_size = '1GB';

-- Increase work_mem
ALTER SYSTEM SET work_mem = '4MB';

-- Reload configuration
SELECT pg_reload_conf();
```

### 3. Redis Tuning

**Redis configuration:**
```conf
# redis.conf
maxmemory 512mb
maxmemory-policy allkeys-lru
save 900 1
save 300 10
save 60 10000
```

## Troubleshooting

### Common Issues

#### 1. Service Discovery Issues
```bash
# Check service registration
curl http://localhost:8761/eureka/apps

# Check service health
curl http://localhost:8080/actuator/health
```

#### 2. Database Connection Issues
```bash
# Test database connectivity
telnet db-host 5432

# Check connection pool
curl http://localhost:8081/actuator/metrics/hikaricp.connections.active
```

#### 3. Cache Issues
```bash
# Test Redis connectivity
redis-cli -h redis-host ping

# Check cache statistics
curl http://localhost:8081/actuator/metrics/cache.gets
```

#### 4. Kafka Issues
```bash
# Check Kafka connectivity
kafka-console-producer --bootstrap-server kafka-host:9092 --topic test-topic

# Check consumer lag
kafka-consumer-groups --bootstrap-server kafka-host:9092 --describe --group inventory-system
```

### Debug Mode

**Enable debug logging:**
```yaml
logging:
  level:
    com.mercadolibre: DEBUG
    org.springframework.cloud.gateway: DEBUG
    org.springframework.kafka: DEBUG
```

**Enable JMX:**
```bash
JAVA_OPTS="$JAVA_OPTS -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=9999 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false"
```

## Rollback Procedures

### 1. Application Rollback

**Kubernetes rollback:**
```bash
# Rollback to previous version
kubectl rollout undo deployment/api-gateway -n inventory-system
kubectl rollout undo deployment/inventory-service -n inventory-system
kubectl rollout undo deployment/sync-service -n inventory-system
```

**Docker rollback:**
```bash
# Rollback to previous image
docker-compose -f docker-compose.prod.yml down
docker-compose -f docker-compose.prod.yml up -d
```

### 2. Database Rollback

**Restore from backup:**
```bash
# Stop services
docker-compose down

# Restore database
psql -h db-host -U db-user -d inventory_prod < /backups/inventory_backup_20240115_020000.sql

# Restart services
docker-compose up -d
```

## Maintenance

### 1. Regular Maintenance Tasks

**Weekly tasks:**
- Review logs for errors
- Check disk space usage
- Verify backup integrity
- Update security patches

**Monthly tasks:**
- Performance review
- Capacity planning
- Security audit
- Documentation updates

### 2. Monitoring Alerts

**Set up alerts for:**
- High CPU usage (>80%)
- High memory usage (>85%)
- Disk space low (<20% free)
- Service down
- High error rate (>5%)
- Slow response time (>2s)

**Alert configuration:**
```yaml
# prometheus-alerts.yml
groups:
- name: inventory-system
  rules:
  - alert: HighCPUUsage
    expr: cpu_usage_percent > 80
    for: 5m
    labels:
      severity: warning
    annotations:
      summary: "High CPU usage detected"
      description: "CPU usage is above 80% for more than 5 minutes"
```
