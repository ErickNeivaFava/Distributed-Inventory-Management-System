# API Documentation

## Overview

This document provides comprehensive API documentation for the Distributed Inventory Management System.

## Base URLs

- **API Gateway**: `http://localhost:8080`
- **Inventory Service**: `http://localhost:8081`
- **Sync Service**: `http://localhost:8082`

## Authentication

All API endpoints require authentication using JWT tokens.

### Headers
```
Authorization: Bearer <jwt_token>
Content-Type: application/json
```

### Example Token
```
Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

## Response Format

All API responses follow a consistent format:

### Success Response
```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... },
  "timestamp": "2024-01-15T10:30:00.000Z"
}
```

### Error Response
```json
{
  "success": false,
  "message": "Error description",
  "errorCode": "ERROR_CODE",
  "timestamp": "2024-01-15T10:30:00.000Z"
}
```

## Inventory Management APIs

### 1. Get Inventory

Retrieve inventory information for a specific product in a store.

**Endpoint**: `GET /api/inventory/{storeId}/{productId}`

**Parameters**:
- `storeId` (path, required): Store identifier
- `productId` (path, required): Product identifier

**Response**:
```json
{
  "success": true,
  "message": "Inventory retrieved successfully",
  "data": {
    "storeId": "store-1",
    "productId": "product-123",
    "quantity": 50,
    "lastUpdated": "2024-01-15T10:30:00.000Z"
  },
  "timestamp": "2024-01-15T10:30:00.000Z"
}
```

**Error Codes**:
- `INVENTORY_NOT_FOUND`: Inventory not found
- `STORE_NOT_FOUND`: Store not found
- `UNAUTHORIZED`: Invalid or missing token

### 2. Get Inventory Across Stores

Retrieve inventory information for a product across all stores.

**Endpoint**: `GET /api/inventory/{productId}`

**Parameters**:
- `productId` (path, required): Product identifier

**Response**:
```json
{
  "success": true,
  "message": "Inventory across stores retrieved successfully",
  "data": [
    {
      "storeId": "store-1",
      "productId": "product-123",
      "quantity": 50,
      "lastUpdated": "2024-01-15T10:30:00.000Z"
    },
    {
      "storeId": "store-2",
      "productId": "product-123",
      "quantity": 25,
      "lastUpdated": "2024-01-15T09:15:00.000Z"
    }
  ],
  "timestamp": "2024-01-15T10:30:00.000Z"
}
```

### 3. Set Inventory

Set the inventory quantity for a specific product in a store.

**Endpoint**: `PUT /api/inventory/{storeId}/{productId}`

**Parameters**:
- `storeId` (path, required): Store identifier
- `productId` (path, required): Product identifier

**Request Body**:
```json
{
  "quantity": 100
}
```

**Response**:
```json
{
  "success": true,
  "message": "Inventory set successfully",
  "data": {
    "storeId": "store-1",
    "productId": "product-123",
    "quantity": 100,
    "lastUpdated": "2024-01-15T10:30:00.000Z"
  },
  "timestamp": "2024-01-15T10:30:00.000Z"
}
```

**Error Codes**:
- `VALIDATION_ERROR`: Invalid request body
- `INVENTORY_NOT_FOUND`: Inventory not found
- `STORE_NOT_FOUND`: Store not found

### 4. Increment Inventory

Increase the inventory quantity for a specific product.

**Endpoint**: `POST /api/inventory/{storeId}/{productId}/increment`

**Parameters**:
- `storeId` (path, required): Store identifier
- `productId` (path, required): Product identifier
- `quantity` (query, required): Amount to increment

**Example**: `POST /api/inventory/store-1/product-123/increment?quantity=10`

**Response**:
```json
{
  "success": true,
  "message": "Inventory incremented successfully",
  "data": {
    "storeId": "store-1",
    "productId": "product-123",
    "quantity": 60,
    "lastUpdated": "2024-01-15T10:30:00.000Z"
  },
  "timestamp": "2024-01-15T10:30:00.000Z"
}
```

### 5. Decrement Inventory

Decrease the inventory quantity for a specific product.

**Endpoint**: `POST /api/inventory/{storeId}/{productId}/decrement`

**Parameters**:
- `storeId` (path, required): Store identifier
- `productId` (path, required): Product identifier
- `quantity` (query, required): Amount to decrement

**Example**: `POST /api/inventory/store-1/product-123/decrement?quantity=5`

**Response**:
```json
{
  "success": true,
  "message": "Inventory decremented successfully",
  "data": {
    "storeId": "store-1",
    "productId": "product-123",
    "quantity": 45,
    "lastUpdated": "2024-01-15T10:30:00.000Z"
  },
  "timestamp": "2024-01-15T10:30:00.000Z"
}
```

**Error Codes**:
- `INSUFFICIENT_INVENTORY`: Not enough inventory available
- `INVENTORY_NOT_FOUND`: Inventory not found

### 6. Get Low Stock Items

Retrieve products with low stock in a specific store.

**Endpoint**: `GET /api/inventory/{storeId}/low-stock`

**Parameters**:
- `storeId` (path, required): Store identifier
- `threshold` (query, optional): Low stock threshold (default: 10)

**Example**: `GET /api/inventory/store-1/low-stock?threshold=5`

**Response**:
```json
{
  "success": true,
  "message": "Low stock items retrieved successfully",
  "data": [
    {
      "storeId": "store-1",
      "productId": "product-456",
      "quantity": 3,
      "lastUpdated": "2024-01-15T10:30:00.000Z"
    },
    {
      "storeId": "store-1",
      "productId": "product-789",
      "quantity": 7,
      "lastUpdated": "2024-01-15T09:45:00.000Z"
    }
  ],
  "timestamp": "2024-01-15T10:30:00.000Z"
}
```

### 7. Get Inventory Summary

Get a summary of inventory statistics for a store.

**Endpoint**: `GET /api/inventory/{storeId}/summary`

**Parameters**:
- `storeId` (path, required): Store identifier

**Response**:
```json
{
  "success": true,
  "message": "Inventory summary retrieved successfully",
  "data": {
    "storeId": "store-1",
    "totalProducts": 150,
    "totalQuantity": 5000,
    "lowStockCount": 12,
    "outOfStockCount": 3,
    "lastUpdated": "2024-01-15T10:30:00.000Z"
  },
  "timestamp": "2024-01-15T10:30:00.000Z"
}
```

## Synchronization APIs

### 1. Trigger Manual Sync

Manually trigger synchronization for a specific store.

**Endpoint**: `POST /api/sync/{storeId}`

**Parameters**:
- `storeId` (path, required): Store identifier

**Response**:
```json
{
  "success": true,
  "message": "Synchronization triggered successfully",
  "data": {
    "syncId": "sync-12345",
    "storeId": "store-1",
    "status": "IN_PROGRESS",
    "startTime": "2024-01-15T10:30:00.000Z"
  },
  "timestamp": "2024-01-15T10:30:00.000Z"
}
```

### 2. Get Sync Status

Retrieve the status of a synchronization operation.

**Endpoint**: `GET /api/sync/status/{syncId}`

**Parameters**:
- `syncId` (path, required): Synchronization identifier

**Response**:
```json
{
  "success": true,
  "message": "Sync status retrieved successfully",
  "data": {
    "syncId": "sync-12345",
    "storeId": "store-1",
    "status": "COMPLETED",
    "startTime": "2024-01-15T10:30:00.000Z",
    "endTime": "2024-01-15T10:32:15.000Z",
    "successCount": 150,
    "failureCount": 0,
    "type": "STORE_SYNC"
  },
  "timestamp": "2024-01-15T10:32:15.000Z"
}
```

### 3. Get All Sync Statuses

Retrieve all synchronization operations.

**Endpoint**: `GET /api/sync/status`

**Response**:
```json
{
  "success": true,
  "message": "All sync statuses retrieved successfully",
  "data": [
    {
      "syncId": "sync-12345",
      "storeId": "store-1",
      "status": "COMPLETED",
      "startTime": "2024-01-15T10:30:00.000Z",
      "endTime": "2024-01-15T10:32:15.000Z",
      "successCount": 150,
      "failureCount": 0,
      "type": "STORE_SYNC"
    },
    {
      "syncId": "sync-12346",
      "storeId": "store-2",
      "status": "IN_PROGRESS",
      "startTime": "2024-01-15T10:35:00.000Z",
      "successCount": 75,
      "failureCount": 2,
      "type": "STORE_SYNC"
    }
  ],
  "timestamp": "2024-01-15T10:35:30.000Z"
}
```

## Health Check APIs

### 1. Service Health

Check the health status of a service.

**Endpoint**: `GET /actuator/health`

**Response**:
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP"
    },
    "redis": {
      "status": "UP"
    },
    "kafka": {
      "status": "UP"
    }
  }
}
```

### 2. Service Info

Get service information and version details.

**Endpoint**: `GET /actuator/info`

**Response**:
```json
{
  "app": {
    "name": "inventory-service",
    "version": "0.0.1-SNAPSHOT",
    "description": "Distributed Inventory Management System"
  },
  "build": {
    "version": "0.0.1-SNAPSHOT",
    "time": "2024-01-15T08:00:00.000Z"
  }
}
```

## Error Codes Reference

| Error Code | HTTP Status | Description |
|------------|-------------|-------------|
| `INVENTORY_NOT_FOUND` | 404 | Inventory record not found |
| `STORE_NOT_FOUND` | 404 | Store not found |
| `PRODUCT_NOT_FOUND` | 404 | Product not found |
| `INSUFFICIENT_INVENTORY` | 400 | Not enough inventory available |
| `VALIDATION_ERROR` | 400 | Request validation failed |
| `UNAUTHORIZED` | 401 | Authentication required |
| `FORBIDDEN` | 403 | Access denied |
| `RATE_LIMIT_EXCEEDED` | 429 | Too many requests |
| `SYNC_CONFLICT` | 409 | Synchronization conflict |
| `SYNC_FAILED` | 500 | Synchronization failed |
| `INTERNAL_ERROR` | 500 | Internal server error |

## Rate Limiting

The API implements rate limiting to prevent abuse:

- **Inventory Operations**: 10 requests per second per user
- **Sync Operations**: 5 requests per second per user
- **Health Checks**: No rate limiting

Rate limit headers are included in responses:
```
X-RateLimit-Limit: 10
X-RateLimit-Remaining: 9
X-RateLimit-Reset: 1642248000
```

## Pagination

For endpoints that return lists, pagination is supported:

**Query Parameters**:
- `page`: Page number (default: 0)
- `size`: Page size (default: 20)
- `sort`: Sort field and direction (e.g., `lastUpdated,desc`)

**Response Headers**:
```
X-Total-Count: 150
X-Page-Size: 20
X-Page-Number: 0
X-Total-Pages: 8
```

## WebSocket Events

Real-time inventory updates are available via WebSocket:

**Connection**: `ws://localhost:8080/ws/inventory`

**Events**:
- `inventory.updated`: Inventory quantity changed
- `inventory.low_stock`: Low stock alert
- `sync.completed`: Synchronization completed
- `sync.failed`: Synchronization failed

**Example Event**:
```json
{
  "type": "inventory.updated",
  "data": {
    "storeId": "store-1",
    "productId": "product-123",
    "quantity": 45,
    "timestamp": "2024-01-15T10:30:00.000Z"
  }
}
```

## SDK Examples

### Java (Spring Boot)
```java
@RestController
public class InventoryClient {
    
    @Autowired
    private RestTemplate restTemplate;
    
    public InventoryResponse getInventory(String storeId, String productId) {
        String url = "http://localhost:8080/api/inventory/" + storeId + "/" + productId;
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("your-jwt-token");
        
        HttpEntity<?> entity = new HttpEntity<>(headers);
        ResponseEntity<ApiResponse<InventoryResponse>> response = 
            restTemplate.exchange(url, HttpMethod.GET, entity, 
                new ParameterizedTypeReference<ApiResponse<InventoryResponse>>() {});
        
        return response.getBody().getData();
    }
}
```

### JavaScript (Node.js)
```javascript
const axios = require('axios');

class InventoryClient {
    constructor(baseURL, token) {
        this.client = axios.create({
            baseURL,
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });
    }
    
    async getInventory(storeId, productId) {
        const response = await this.client.get(`/api/inventory/${storeId}/${productId}`);
        return response.data.data;
    }
    
    async updateInventory(storeId, productId, quantity) {
        const response = await this.client.put(`/api/inventory/${storeId}/${productId}`, {
            quantity
        });
        return response.data.data;
    }
}
```

### Python
```python
import requests

class InventoryClient:
    def __init__(self, base_url, token):
        self.base_url = base_url
        self.headers = {
            'Authorization': f'Bearer {token}',
            'Content-Type': 'application/json'
        }
    
    def get_inventory(self, store_id, product_id):
        url = f"{self.base_url}/api/inventory/{store_id}/{product_id}"
        response = requests.get(url, headers=self.headers)
        return response.json()['data']
    
    def update_inventory(self, store_id, product_id, quantity):
        url = f"{self.base_url}/api/inventory/{store_id}/{product_id}"
        data = {'quantity': quantity}
        response = requests.put(url, json=data, headers=self.headers)
        return response.json()['data']
```

## Testing

### Postman Collection
Import the provided Postman collection for easy API testing:
- Environment variables for base URLs and tokens
- Pre-configured requests for all endpoints
- Test scripts for response validation

### cURL Examples
```bash
# Get inventory
curl -H "Authorization: Bearer your-token" \
     http://localhost:8080/api/inventory/store-1/product-123

# Update inventory
curl -X PUT \
     -H "Authorization: Bearer your-token" \
     -H "Content-Type: application/json" \
     -d '{"quantity": 100}' \
     http://localhost:8080/api/inventory/store-1/product-123

# Decrement inventory
curl -X POST \
     -H "Authorization: Bearer your-token" \
     "http://localhost:8080/api/inventory/store-1/product-123/decrement?quantity=5"
```
