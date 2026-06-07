# Demo — User & Product Management API

A Spring Boot 3 REST API with JWT-based authentication, role-based authorization, and rate limiting.

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 17 |
| Framework | Spring Boot 3.2.5 |
| Security | Spring Security 6 + external Auth Service |
| Database | MySQL 8 + Spring Data JPA |
| Rate Limiting | Bucket4j 8.10.1 |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Testing | JUnit 5 + Mockito + Spring Security Test |
| Build | Maven |

---

## Project Structure

```
src/main/java/com/example/demo/
├── client/         AuthServiceClient, AuthResult
├── config/         SecurityConfig, AppConfig, OpenApiConfig
├── controller/     UserController, ProductController
├── dto/            Request / Response DTOs
├── exception/      GlobalExceptionHandler, custom exceptions
├── filter/         AuthTokenFilter, RateLimitFilter
├── mapper/         UserMapper, ProductMapper
├── model/          User, Product (JPA entities)
├── repository/     UserRepository, ProductRepository
└── service/        UserService, ProductService (+ impl/)
```

---

## Getting Started

### Prerequisites

- Java 17+
- MySQL 8 running on `localhost:3306`
- External Auth Service running on `localhost:4001`

### 1. Configure local credentials

Copy the local properties file (already created — do **not** commit it):

```
src/main/resources/application-local.properties
```

Edit it with your MySQL credentials:

```properties
DB_URL=jdbc:mysql://localhost:3306/demo?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true
DB_USERNAME=your_username
DB_PASSWORD=your_password
```

### 2. Run the application

```bash
./mvnw spring-boot:run
```

The `local` profile is active by default — `application-local.properties` is picked up automatically.

### 3. Access the API

| URL | Description |
|-----|-------------|
| `http://localhost:8080/api/users` | Users API |
| `http://localhost:8080/api/products` | Products API |
| `http://localhost:8080/swagger-ui.html` | Swagger UI |

---

## API Overview

### Users `/api/users`

| Method | Endpoint | Auth Required | Role |
|--------|----------|--------------|------|
| GET | `/api/users` | Yes | ADMIN |
| GET | `/api/users/{id}` | Yes | Any |
| POST | `/api/users` | Yes | Any |
| PUT | `/api/users/{id}` | Yes | Any |
| DELETE | `/api/users/{id}` | Yes | ADMIN |

### Products `/api/products`

| Method | Endpoint | Auth Required | Role |
|--------|----------|--------------|------|
| GET | `/api/products` | No | — |
| GET | `/api/products/{id}` | No | — |
| GET | `/api/products/search/**` | Yes | Any |
| POST | `/api/products` | Yes | Any |
| PUT | `/api/products/{id}` | Yes | Any |
| PATCH | `/api/products/{id}` | Yes | Any |
| DELETE | `/api/products/{id}` | Yes | ADMIN |

### Authentication

All protected endpoints require a Bearer token in the `Authorization` header:

```
Authorization: Bearer <token>
```

Tokens are validated by calling the external auth service at `http://localhost:4001/api/v1/auth/token/validate`.

---

## Security Layers

| Layer | What it does |
|-------|-------------|
| **RateLimitFilter** | 30 requests/min per IP — runs first, before Spring Security |
| **CorsFilter** | Allows requests only from `localhost:3000` and `localhost:8080` |
| **AuthTokenFilter** | Validates Bearer token, extracts subject and roles into SecurityContext |
| **AuthorizationFilter** | Enforces per-endpoint access rules (authenticated / ROLE_ADMIN) |
| **Security Headers** | HSTS, X-Frame-Options: DENY, X-Content-Type-Options |
| **Bean Validation** | `@Valid` on all request bodies — returns 400 on invalid input |

---

## Running Tests

```bash
./mvnw test
```

Coverage report (JaCoCo):

```bash
./mvnw verify
# Report at: target/site/jacoco/index.html
```

---

## Environment Variables (Production)

When deploying, set these instead of using `application-local.properties`:

| Variable | Description |
|----------|-------------|
| `DB_URL` | JDBC connection URL |
| `DB_USERNAME` | Database username |
| `DB_PASSWORD` | Database password |
| `SPRING_PROFILES_ACTIVE` | Set to `prod` (or any non-`local` value) |

---

## Known Gaps (To Fix Before Production)

- [ ] IDOR on `GET /api/users/{id}` — any authenticated user can read any other user's data
- [ ] Swagger UI is publicly accessible — restrict to ADMIN or disable
- [ ] No HTTPS — all traffic including Bearer tokens is unencrypted
- [ ] MySQL connection uses `useSSL=false`
