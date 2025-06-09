
# SpringOps – Deployment Management Platform

## Overview

SpringOps is a backend platform for managing and deploying Java applications. It supports:

- Cloning Git repositories using personal access tokens
- Building Java applications with Maven
- Deploying applications using `java -jar`
- Managing environment variables per application
- Tracking deployment metadata and system configurations

The project is built with **Spring Boot 3.4.5**, uses **PostgreSQL**, and handles database versioning through **Flyway**.

## Features

- JWT-based Authentication
- Application CRUD with Environment Variables
- Custom Maven and Java version management
- Branch-based deployment tracking
- Shell-based automation scripts
- Role-based security
- Startup bootstrap for first-time setup

## Technology Stack

- Java 21
- Spring Boot 3.4.5
- Spring Security
- Spring Data JPA
- PostgreSQL
- Flyway
- JWT (jjwt)
- Lombok
- Shell Scripting

## Modules

### Authentication

Handles user authentication and JWT token issuance.

**Key Components:**

- `User`, `UserRepository`, `UserService`
- `AuthenticationController`
- `JwtAuthenticationFilter`, `JwtUtil`, `JwtConfig`
- `SecurityConfig`, `PasswordEncoderConfig`

### Application Management

Each application has a Maven and Java system version, Git URL, and root folder.

**Entities:**

- `Application`
- `ApplicationRepository`, `ApplicationService`, `ApplicationController`
- `ApplicationEnv` (name-value pairs for environment config)

**Endpoints:**

- `/api/applications`
- `/api/applications/env`

### Environment Variables

Stored in the `application_env` table and injected at runtime during deployment.

```sql
CREATE TABLE application_env (
  id SERIAL PRIMARY KEY,
  application_id INT NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
  name VARCHAR(255) NOT NULL,
  value TEXT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Deployment Management

Handles application deployments via Git, Maven, and runtime configuration.

**Entities:**

- `Deployment`
- `DeploymentRepository`, `DeploymentService`, `DeploymentController`
- `DeploymentManagerService`, `DeploymentManagerController`

**Endpoints:**

- `/api/deployments`
- `/api/deployments/manager`

### Version Management

Tracks installed Maven and Java versions on the host.

```sql
CREATE TABLE system_versions (
    id SERIAL PRIMARY KEY,
    type VARCHAR(20) NOT NULL,
    version VARCHAR(50) NOT NULL,
    path TEXT NOT NULL,
    name VARCHAR(255) NOT NULL DEFAULT 'default',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Setup & Bootstrap

Handles server setup metadata and startup bootstrapping.

**Entities:**

- `Setup`, `StartupInitializer`
- `SetupRepository`, `SetupService`, `SetupController`

## Shell Scripts

### `update_project.sh`

Clones a specified Git branch into a target directory and creates a deployment branch.

```bash
./update_project.sh <GIT_URL> <BRANCH> <CLONE_DIR>
```

### `build_project.sh`

Builds a project with a specified Maven and Java version.

```bash
./build_project.sh <JAVA_BIN_PATH> <MAVEN_BIN_PATH> <PROJECT_DIR> <JAVA_VERSION>
```

### `run_project.sh`

Runs a `.jar` file with exported environment variables.

```bash
./run_project.sh <JAVA_PATH> <PROJECT_DIR> <JAR_NAME> VAR1=value1 VAR2=value2 ...
```

## Configuration

### `application.yml`

Contains general Spring Boot configuration:
- Server port
- JWT secret
- Database credentials
- File path settings

### `pom.xml`

Spring Boot 3.4.5 with dependencies for:
- Web, Security, JPA, Flyway
- PostgreSQL, JWT
- Lombok, Hibernate Validator

## Database Schema (Flyway Migrations)

- V1__create_setup_table.sql
- V2__create_user_table.sql
- V3__create_system_versions_table.sql
- V4__update_system_versions_table.sql
- V5__create_applications_table.sql
- V6__create_application_env_table.sql
- V7__create_deployments_table.sql
- V8__update_deployments_table.sql
- V9__update_setup_table.sql

## Deployment Workflow

1. Configure Git access token and environment variables in DB.
2. Pull specified Git branch using `update_project.sh`.
3. Build project using `build_project.sh`.
4. Run `.jar` with injected env variables via `run_project.sh`.
5. Track PID and deployment info in `deployments` table.
