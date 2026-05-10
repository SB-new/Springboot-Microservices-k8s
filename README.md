# Inventory Management API

Production-patterned REST API built with **Spring Boot 3**, **Java 21**, **PostgreSQL**, and **JWT authentication**.

---

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        Client (HTTP)                            │
└─────────────────────────┬───────────────────────────────────────┘
                          │ HTTPS
                          ▼
┌─────────────────────────────────────────────────────────────────┐
│              Ingress / Reverse Proxy (nginx)                    │
└─────────────────────────┬───────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Spring Boot 3 App                            │
│                                                                 │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────────┐  │
│  │ JwtAuthFilter│───▶│  Controller  │───▶│    Service       │  │
│  │ (Security 6) │    │  (REST layer)│    │ (Business logic) │  │
│  └──────────────┘    └──────────────┘    └────────┬─────────┘  │
│                                                   │            │
│  ┌──────────────┐                       ┌─────────▼──────────┐ │
│  │  JwtService  │                       │    Repository      │ │
│  │  (jjwt 0.12) │                       │  (Spring Data JPA) │ │
│  └──────────────┘                       └─────────┬──────────┘ │
│                                                   │            │
│  ┌──────────────────────────────────────┐         │            │
│  │       GlobalExceptionHandler         │         │            │
│  │  (404/409/400/401/403/500 mapping)   │         │            │
│  └──────────────────────────────────────┘         │            │
└──────────────────────────────────────────────────-┼────────────┘
                                                    │
                          ┌─────────────────────────▼──────────────┐
                          │         PostgreSQL 16                   │
                          │  ┌─────────┐ ┌──────────┐ ┌─────────┐ │
                          │  │  users  │ │ products │ │ inv_items│ │
                          │  └─────────┘ └──────────┘ └─────────┘ │
                          │      Flyway migrations (V1–V3)         │
                          └────────────────────────────────────────┘
```

---

## Tech Stack

| Layer        | Technology                          |
|--------------|-------------------------------------|
| Language     | Java 21 (virtual threads ready)     |
| Framework    | Spring Boot 3.2                     |
| Security     | Spring Security 6 + JWT (jjwt 0.12) |
| Persistence  | Spring Data JPA + Hibernate 6       |
| Database     | PostgreSQL 16                       |
| Migrations   | Flyway 10                           |
| Build        | Maven 3.9                           |
| Container    | Docker (eclipse-temurin:21-jre-alpine)|
| Orchestration| Kubernetes                          |
| CI/CD        | GitHub Actions → GHCR               |

---

## Project Structure

```
src/
├── main/
│   ├── java/com/inventory/
│   │   ├── InventoryApplication.java
│   │   ├── config/
│   │   │   ├── ApplicationConfig.java    # Beans: UserDetailsService, PasswordEncoder, AuthMgr
│   │   │   └── SecurityConfig.java       # SecurityFilterChain, role-based rules
│   │   ├── controller/
│   │   │   ├── AuthController.java       # POST /api/auth/register|login
│   │   │   ├── ProductController.java    # CRUD /api/products
│   │   │   └── InventoryController.java  # CRUD /api/inventory
│   │   ├── dto/
│   │   │   ├── auth/   RegisterRequest, AuthRequest, AuthResponse
│   │   │   ├── product/ ProductRequest, ProductResponse
│   │   │   └── inventory/ InventoryRequest, InventoryResponse
│   │   ├── entity/
│   │   │   ├── Role.java (enum USER|ADMIN)
│   │   │   ├── User.java (implements UserDetails)
│   │   │   ├── Product.java
│   │   │   └── InventoryItem.java
│   │   ├── exception/
│   │   │   ├── GlobalExceptionHandler.java
│   │   │   ├── ErrorResponse.java
│   │   │   ├── ResourceNotFoundException.java
│   │   │   └── DuplicateResourceException.java
│   │   ├── repository/
│   │   │   ├── UserRepository.java
│   │   │   ├── ProductRepository.java
│   │   │   └── InventoryItemRepository.java
│   │   ├── security/
│   │   │   ├── JwtService.java           # Token generation & validation
│   │   │   └── JwtAuthFilter.java        # OncePerRequestFilter
│   │   └── service/
│   │       ├── AuthService.java
│   │       ├── ProductService.java
│   │       └── InventoryService.java
│   └── resources/
│       ├── application.yml
│       ├── application-dev.yml
│       ├── application-prod.yml
│       └── db/migration/
│           ├── V1__create_users_table.sql
│           ├── V2__create_products_table.sql
│           └── V3__create_inventory_items_table.sql
├── test/java/com/inventory/
│   ├── InventoryApplicationTests.java    # Testcontainers integration test
│   ├── controller/AuthControllerTest.java
│   └── service/InventoryServiceTest.java
k8s/
├── configmap.yaml
├── secret.yaml
├── deployment.yaml
├── service.yaml
└── ingress.yaml
.github/workflows/ci.yml
Dockerfile
docker-compose.yml
```

---

## API Reference

### Auth — public endpoints

| Method | Path                  | Body                              | Response        | Description        |
|--------|-----------------------|-----------------------------------|-----------------|--------------------|
| POST   | `/api/auth/register`  | `{username, email, password}`     | `{token}` 201   | Register new user  |
| POST   | `/api/auth/login`     | `{username, password}`            | `{token}` 200   | Authenticate       |

### Products — `Authorization: Bearer <token>` required

| Method | Path               | Role        | Body                              | Response              | Description         |
|--------|--------------------|-------------|-----------------------------------|-----------------------|---------------------|
| GET    | `/api/products`    | USER, ADMIN | —                                 | Page\<ProductResponse\> 200 | List all (paged) |
| GET    | `/api/products/{id}` | USER, ADMIN | —                               | `ProductResponse` 200 | Get by ID           |
| POST   | `/api/products`    | ADMIN       | `{name, sku, description, price}` | `ProductResponse` 201 | Create product      |
| PUT    | `/api/products/{id}` | ADMIN     | `{name, sku, description, price}` | `ProductResponse` 200 | Update product      |
| DELETE | `/api/products/{id}` | ADMIN     | —                                 | 204 No Content        | Delete product      |

### Inventory — `Authorization: Bearer <token>` required

| Method | Path                              | Role        | Body                                  | Response               | Description              |
|--------|-----------------------------------|-------------|---------------------------------------|------------------------|--------------------------|
| GET    | `/api/inventory`                  | USER, ADMIN | —                                     | Page\<InventoryResponse\> 200 | List all (paged)  |
| GET    | `/api/inventory/{id}`             | USER, ADMIN | —                                     | `InventoryResponse` 200| Get item by ID           |
| GET    | `/api/inventory/product/{pid}`    | USER, ADMIN | —                                     | `List<InventoryResponse>` 200 | Items for a product |
| GET    | `/api/inventory/low-stock?threshold=10` | USER, ADMIN | —                              | `List<InventoryResponse>` 200 | Items below threshold |
| POST   | `/api/inventory`                  | ADMIN       | `{productId, quantity, location}`     | `InventoryResponse` 201| Create inventory item    |
| PUT    | `/api/inventory/{id}`             | ADMIN       | `{productId, quantity, location}`     | `InventoryResponse` 200| Update inventory item    |
| DELETE | `/api/inventory/{id}`             | ADMIN       | —                                     | 204 No Content         | Delete inventory item    |

### Error Response Shape

```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Product not found with id: 42",
  "path": "/api/products/42",
  "fieldErrors": null
}
```

---

## Role-Based Access

| Role  | Can do                                                  |
|-------|---------------------------------------------------------|
| USER  | Read products, read inventory, view low-stock report    |
| ADMIN | Everything USER can do + create/update/delete all resources |

New accounts registered via `/api/auth/register` receive the **USER** role. To promote an account to ADMIN, update the `role` column directly in the database.

---

## Quick Start

### Prerequisites

- Java 21+
- Maven 3.9+
- Docker & Docker Compose
- PostgreSQL 16 (or use Docker Compose)

### 1. Clone & configure

```bash
git clone https://github.com/YOUR_ORG/inventory-api.git
cd inventory-api
```

### 2. Run with Docker Compose (recommended)

```bash
# Builds the image and starts app + postgres
docker compose up --build

# App available at http://localhost:8080
```

### 3. Run locally (dev profile)

```bash
# Start PostgreSQL
docker run -d \
  -e POSTGRES_DB=inventory_db \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:16-alpine

# Run the app (Flyway migrates automatically)
mvn spring-boot:run
```

### 4. Generate a production JWT secret

```bash
openssl rand -base64 32
# Paste output as JWT_SECRET env var
```

### 5. Run tests

```bash
# Unit tests only (no Docker needed)
mvn test -Dtest="InventoryServiceTest,AuthControllerTest"

# All tests including integration (Docker required for Testcontainers)
mvn test
```

---

## Kubernetes Deployment

```bash
# 1. Build & push image to GHCR
docker build -t ghcr.io/YOUR_ORG/inventory-api:latest .
docker push ghcr.io/YOUR_ORG/inventory-api:latest

# 2. Update image in k8s/deployment.yaml
sed -i 's|YOUR_GITHUB_ORG|your-actual-org|g' k8s/deployment.yaml

# 3. Update secrets (base64-encode real values)
kubectl apply -f k8s/secret.yaml
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
kubectl apply -f k8s/ingress.yaml

# 4. Check rollout
kubectl rollout status deployment/inventory-api
```

---

## CI/CD Pipeline

The GitHub Actions workflow (`.github/workflows/ci.yml`) runs on every push to `main`:

```
push to main
    │
    ▼
┌─────────────┐     ┌──────────────────┐     ┌──────────────────────┐
│  Unit Tests  │────▶│  Maven Package   │────▶│  Docker Build+Push   │
│  (+ Postgres │     │  (DskipTests)    │     │  → GHCR              │
│   service)   │     └──────────────────┘     │  tags: sha, branch,  │
└─────────────┘                               │        latest        │
                                              └──────────────────────┘
```

Images are tagged with `sha-<commit>`, branch name, and `latest`.

---

## Pagination

All list endpoints support standard Spring `Pageable` query params:

```
GET /api/products?page=0&size=20&sort=name,asc
GET /api/inventory?page=1&size=10&sort=lastUpdated,desc
```

---

## Environment Variables

| Variable              | Default (dev)                        | Required in prod |
|-----------------------|--------------------------------------|-----------------|
| `SPRING_PROFILES_ACTIVE` | `dev`                             | yes             |
| `DB_URL`              | `jdbc:postgresql://localhost:5432/inventory_db` | yes |
| `DB_USERNAME`         | `postgres`                           | yes             |
| `DB_PASSWORD`         | `postgres`                           | yes             |
| `JWT_SECRET`          | built-in dev default (NOT secure)    | yes             |
| `JWT_EXPIRATION_MS`   | `86400000` (24 h)                    | no              |
| `SERVER_PORT`         | `8080`                               | no              |

