# Reactive Redis POC — Spring Boot WebFlux

A production-ready proof-of-concept demonstrating **Reactive Redis** with Spring Boot WebFlux using Project Reactor (`Mono` / `Flux`).

---

## 🧱 Tech Stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot 3.2, Spring WebFlux |
| Redis Client | Lettuce (non-blocking, reactive) |
| Reactive Lib | Project Reactor (`Mono`, `Flux`) |
| Serialization | Jackson (JSON) |
| Build | Maven |
| Testing | JUnit 5, Mockito, `reactor-test` `StepVerifier` |

---

## 📁 Project Structure

```
src/main/java/com/poc/reactiveredis/
├── ReactiveRedisApplication.java     # Entry point
├── config/
│   ├── RedisConfig.java              # ReactiveRedisTemplate beans, serializers
│   ├── RedisSubscriberConfig.java    # Reactive Pub/Sub subscriber
│   ├── GlobalExceptionHandler.java   # Centralized error handling
│   └── DataInitializer.java          # Seed data on startup
├── controller/
│   └── ProductController.java        # REST + SSE endpoints
├── model/
│   └── Product.java                  # Domain model (Lombok + Jackson)
├── repository/
│   └── ProductRepository.java        # Raw Redis ops (opsForValue, opsForSet)
└── service/
    ├── ProductService.java           # Business logic, Mono/Flux composition
    └── RedisPublisherService.java    # Pub/Sub publisher
```

---

## 🚀 Running Locally

### 1. Start Redis

```bash
docker-compose up -d
```

This starts:
- **Redis 7.2** on `localhost:6379`
- **RedisInsight** UI on `http://localhost:8001`

### 2. Run the Application

```bash
./mvnw spring-boot:run
```

App starts on `http://localhost:8080`. Sample products are seeded automatically.

---

## 🔌 REST API

### Create Product
```http
POST /api/products
Content-Type: application/json

{
  "name": "Mechanical Keyboard",
  "description": "TKL layout, Cherry MX switches",
  "price": 149.99,
  "category": "Peripherals",
  "stock": 75
}
```

### Get Product by ID
```http
GET /api/products/{id}
```

### Get All Products
```http
GET /api/products
```

### Stream Products (SSE)
```http
GET /api/products/stream
Accept: text/event-stream
```

### Update Product
```http
PUT /api/products/{id}
Content-Type: application/json

{ "name": "Updated Name", "price": 199.99, ... }
```

### Delete Product
```http
DELETE /api/products/{id}
```

### Set TTL
```http
PUT /api/products/{id}/ttl?seconds=600
```

### Get TTL
```http
GET /api/products/{id}/ttl
```

---

## 🔑 Redis Key Design

| Key | Type | Purpose |
|---|---|---|
| `product:{id}` | String (JSON) | Individual product with TTL |
| `product:ids` | Set | Index of all product IDs |
| `product-events` | Pub/Sub channel | Event notifications |

---

## 📡 Reactive Concepts Demonstrated

| Concept | Where |
|---|---|
| `Mono<T>` | Single-value async ops (findById, save) |
| `Flux<T>` | Multi-value streams (findAll, SSE) |
| `flatMap` | Chaining async Redis calls |
| `switchIfEmpty` | Handling empty results (404) |
| `delayElements` | Simulating SSE streaming delay |
| `StepVerifier` | Testing reactive pipelines |
| Pub/Sub | `RedisPublisherService` + `RedisSubscriberConfig` |
| TTL management | `expire()`, `getExpire()` |
| JSON serialization | `Jackson2JsonRedisSerializer` |

---

## 🧪 Running Tests

```bash
./mvnw test
```

Tests use Mockito for unit-level isolation and `StepVerifier` for reactive assertion.

---

## ⚙️ Configuration

```yaml
# application.yml
spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
      lettuce:
        pool:
          max-active: 10
```

Override via environment variables for Docker/Kubernetes deployments.
# reactive-redis-poc
