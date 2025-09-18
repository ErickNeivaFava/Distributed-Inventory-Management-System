# Prompts Utilizados para Desenvolvimento com IA

## Prompt 1: Definição da Stack Tecnológica
```
Preciso desenvolver um sistema distribuído de gestão de inventário para varejo. 
Requisitos: baixa latência, consistência de dados, tolerância a falhas, observabilidade.
Sugira uma stack tecnológica moderna com: framework backend, message broker, cache, monitoring e containerização.
Justifique cada escolha para o contexto de varejo com múltiplas lojas.
```

**Como usei**: Usei a sugestão da IA para definir Java/Spring Boot como core, Kafka para eventos, Redis para cache, e Docker para containerização.

## Prompt 2: Estrutura de Projetos
```
Gere a estrutura de pastas para um sistema de inventory management com os seguintes serviços (Spring Boot):
- inventory-service 
- api-gateway
- eureka-server
- common-lib
- monitoring (Prometheus/Grafana)
Inclua Dockerfiles e docker-compose para orquestração.
```

**Como usei**: A IA gerou a estrutura base que adaptei para o projeto, criando os diretórios principais e arquivos de configuração.

## Prompt 3: Modelagem de Entidades
```
Crie as entidades JPA para um sistema de inventário de varejo:
- Inventory
- Store
Inclua annotations JPA, construtores, getters/setters.
```

**Como usei**: Usei as entidades geradas como base, ajustando campos e relações conforme necessidades específicas.

## Prompt 4: Implementação de Serviços
```
Implemente um InventoryService Spring Boot com alguns endpoins úteis como:
- GET /api/inventory/store/{storeId}
Use Redis para cache com @Cacheable. 
Implemente Circuit Breaker com Resilience4j para tolerância a falhas.
Inclua tratamento de exceções com @ControllerAdvice.
```

**Como usei**: A IA gerou o código base do service que refinei com lógica específica de negócio e tratamento de erros.

## Prompt 5: Configuração Kafka
```
Configure Spring Kafka para:
- Producer para enviar updates de inventario
- Consumer para processar atualizações de inventário
- Tópico "inventory-updates" 
Inclua KafkaTemplate configuration e @KafkaListener.
```

**Como usei**: Usei a configuração gerada como base e ajustei para o formato específico dos meus eventos.

## Prompt 6: Dockerização
```
Gere Dockerfiles para:
- Spring Boot app com Java 21, Maven build multi-stage
- Kafka com Zookeeper
- Redis
- Prometheus
- Grafana
E docker-compose.yml para orquestrar todos os serviços com network própria.
```

**Como usei**: Usei os Dockerfiles gerados e adaptei as versões e configurações para meu ambiente específico.

## Prompt 7: Monitoramento
```
Configure Micrometer com Prometheus para Spring Boot:
- Exponha métricas de endpoints REST
- Métricas de cache Redis
- Health checks
- Custom metrics para inventory updates
Gere dashboard Grafana para monitorar latência, erros e stock updates.
```

**Como usei**: Implementei as configurações de monitoring baseadas no template gerado pela IA.


## Prompt 8: Documentação
```
Gere documentação para endpoints, colando o código da controller
```

**Como usei**: Usei a documentação gerada para criar meus arquivos incluir no projeto.

## Estratégia de Uso de IA:
1. **Geração de Boilerplate**: Usei IA para criar código repetitivo e configurações padrão
2. **Validação de Arquitetura**: Consultei a IA para validar decisões de design distribuído
3. **Otimização de Performance**: Solicitei sugestões para caching e concorrência
4. **Resolução de Problemas**: Usei IA para debug de configurações Kafka e Docker
5. **Documentação**: Gerei documentação técnica através de prompts específicos

A IA acelerou significativamente o desenvolvimento, permitindo focar na lógica de negócio específica enquanto automatizava tarefas rotineiras de configuração e infraestrutura.