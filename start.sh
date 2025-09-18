#!/bin/bash

# Build and start all services
echo "Building and starting Inventory Management System..."
docker-compose up --build -d

echo "Waiting for services to start..."
sleep 30

# Check if all services are running
echo "Checking service status..."
docker-compose ps

echo "Inventory Management System is running!"
echo "API Gateway: http://localhost:8080"
echo "Eureka Server: http://localhost:8761"
echo "Grafana: http://localhost:3000 (admin/admin)"
echo "Prometheus: http://localhost:9090"