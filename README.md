# SpringOps Documentation

## Architecture Overview

SpringOps is a modular Spring Boot application designed for managing and deploying Spring Boot microservices. It includes modules for Git integration, SSH-based deployment, monitoring, audit logging, user authentication, and more.

### Main Modules
- **core**: Main business logic for application management, deployment, logging, etc.
- **auth**: User authentication and JWT handling
- **email**: Notification system
- **audits**: System audits and activity logging
- **scheduled**: Background scheduled tasks
- **setup**: System initialization and environment configuration

---

## Setup & Deployment

### Prerequisites
- Java 17+
- Maven 3.8+
- Docker
- Git

### Run Locally

```bash
cp .env.example .env
mvn spring-boot:run
```

### Build for Production

```bash
mvn clean package -DskipTests
```

---

## API Reference

See individual module files in `/docs/api/`.

---

## Internal Documentation

- See `/docs/modules/` for service, entity, and configuration breakdown.

---

## Configuration

- `.env`: environment variables
- `application.yml`: Spring configurations (profile-based)
- CORS: Global access via `@CrossOrigin`

---

## Testing

```bash
mvn test
```

Unit and integration tests use Spring Boot Test with `@MockBean` for mocking service layers.

---

## Directory Structure

- `src/main/java/org/kreyzon/springops`: Core source code
- `springops-ui`: Angular frontend
- `deploy`: Git-based deploy system
