# SMCCCMS Backend

Small Claims Court Council Management System - Backend API

## Quick Start

1. **Start Database:**
   ```bash
   cd ops && docker-compose -f docker-compose.db.yml up -d
   ```

2. **Run Backend:**
   ```bash
   cd smcccms_be
   ./mvnw spring-boot:run
   ```

3. **Open Swagger UI:**
   - http://localhost:8080/api/swagger-ui/index.html

## Authentication Endpoints

### 1. Verify Government ID
```bash
curl -X POST http://localhost:8080/api/auth/verify-id \
  -H "Content-Type: application/json" \
  -d '{"govId": "ID-UK-001"}'
```

### 2. Request Verification Code
```bash
curl -X POST http://localhost:8080/api/auth/request-code \
  -H "Content-Type: application/json" \
  -d '{"contact": "test@example.com"}'
```

### 3. Verify Code & Create Session
```bash
curl -X POST http://localhost:8080/api/auth/verify-code \
  -H "Content-Type: application/json" \
  -d '{"code": "123456"}' \
  -c cookies.txt
```

## Configuration

Environment variables:
- `DB_USERNAME` (default: smcccms_admin)  
- `DB_PASSWORD` (default: SMCCCMSdemo)
- `USER_PROVIDER_BASE_URL` (default: http://localhost:8081)
- `USER_PROVIDER_USERNAME` (default: demo)
- `USER_PROVIDER_PASSWORD` (default: demo)
- `SESSION_SECRET` (default: demo key - change in production!)

## Tech Stack

- Spring Boot 3.2.12
- Java 21
- PostgreSQL 16 (Docker)
- JDBC (no JPA)
- Flyway migrations
- SpringDoc OpenAPI

See [examples/auth-flow-curl.md](../examples/auth-flow-curl.md) for complete authentication flow examples.