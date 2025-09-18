# run.md

## Como Executar o Sistema de Inventário Distribuído

### Pré-requisitos
- Java 21+ instalado
- Maven 3.8+
- Docker e Docker Compose
- Redis (via Docker)
- Kafka (via Docker)

### 1. Clone o Repositório
```bash
git clone https://github.com/ErickNeivaFava/Distributed-Inventory-Management-System
cd Distributed-Inventory-Management-System
```

### 2. Inicie a Infraestrutura
```bash
docker-compose up -d
```

Isso inicia:
- Redis na porta 6379
- Zookeeper na porta 2181
- Kafka na porta 9092

### 3. Construa e Execute os Serviços

#### Construir Todos os Serviços
```bash
mvn clean install
```

#### Executar Serviços Individualmente

**Servidor Eureka (Porta 8761):**
```bash
cd eureka-service
mvn spring-boot:run -Dspring-boot.run.main-class=com.mercadolibre.sync.EurekaServerApplication
```

**API Gateway (Porta 8080):**
```bash
cd api-gateway
mvn spring-boot:run -Dspring-boot.run.main-class=com.mercadolibre.gateway.ApiGatewayApplication
```

**Serviço de Inventário (Porta 8081):**
```bash
cd inventory-service
mvn spring-boot:run -Dspring-boot.run.main-class=com.mercadolibre.inventory.InventoryServiceApplication
```

**Serviço de Sincronização (Porta 8082):**
```bash
cd sync-service
mvn spring-boot:run -Dspring-boot.run.main-class=com.mercadolibre.sync.SyncServiceApplication
```

### 4. Verifique os Serviços
- **Eureka Server**: http://localhost:8761
- **API Gateway Health**: http://localhost:8080/actuator/health
- **Inventory Service Health**: http://localhost:8081/actuator/health
- **Sync Service Health**: http://localhost:8082/actuator/health

### 5. Teste a API
```bash
# Obter inventário
curl -H "Authorization: Bearer <token>" http://localhost:8080/api/inventory/store-1/product-1

# Atualizar inventário
curl -X PUT -H "Content-Type: application/json" -H "Authorization: Bearer <token>" \
  -d '{"quantity": 100}' http://localhost:8080/api/inventory/store-1/product-1
```

### Comando Rápido (Desenvolvimento)
```bash
# Iniciar tudo com uma linha
docker-compose up -d && mvn clean install && \
cd eureka-server && mvn spring-boot:run & \
cd api-gateway && mvn spring-boot:run & \
cd inventory-service && mvn spring-boot:run & \
cd sync-service && mvn spring-boot:run
```

### Variáveis de Ambiente (Opcional)
```bash
# Configurar variáveis se necessário
export SPRING_DATASOURCE_URL=jdbc:sqlite:inventory.db
export SPRING_DATA_REDIS_HOST=localhost
export SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

### Parar o Sistema
```bash
# Parar serviços Java (Ctrl+C em cada terminal)
# Parar containers Docker
docker-compose down
```

### Troubleshooting Rápido
- Verifique se todos os containers Docker estão rodando: `docker ps`
- Confirme se os serviços estão registrados no Eureka: http://localhost:8761
- Verifique logs em caso de erro: `docker-compose logs`

O sistema estará pronto para uso após todos os serviços estarem em execução e registrados no Eureka Server.