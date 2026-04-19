# Spring Identity Template

![CI](https://github.com/your-org/spring-identity-template/actions/workflows/ci.yml/badge.svg)
![Java](https://img.shields.io/badge/Java-21-ED8B00)
![Kotlin](https://img.shields.io/badge/Kotlin-2.2.21-7F52FF)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.0.3-6DB33F)
![version](https://img.shields.io/badge/version-v0.0.0-orange)

A production-ready Spring Boot identity service template covering authentication, user management, and all the infrastructure scaffolding needed to deploy to production.

## Getting started

```bash
# 1. Click "Use this template" on GitHub, clone your repo, then:
bash scripts/init.sh

# 2. Configure your environment
cp .env.example .env

# 3. Start the database
docker-compose up -d

# 4. Run
./gradlew bootRun
```

API: `http://localhost:8080` — Swagger UI: `http://localhost:8080/swagger-ui.html` (dev profile only)

## What's included

- Email/password auth, Google OAuth (extensible), password reset via email
- JWT access tokens (15 min) + opaque refresh tokens with rotation and reuse detection
- Clean architecture (domain / application / infrastructure), one use case per action
- PostgreSQL + Flyway migrations, Spring Security 6, Actuator, Prometheus, structured logging

| Method  | Path                    | Auth   | Description                  |
|---------|-------------------------|--------|------------------------------|
| `POST`  | `/auth/register`        | Public | Register with email/password |
| `POST`  | `/auth/login`           | Public | Login with email/password    |
| `POST`  | `/auth/oauth/login`     | Public | Login with OAuth provider    |
| `POST`  | `/auth/refresh`         | Public | Rotate refresh token         |
| `POST`  | `/auth/logout`          | Public | Revoke refresh token         |
| `POST`  | `/auth/forgot-password` | Public | Request password reset email |
| `POST`  | `/auth/reset-password`  | Public | Reset password with token    |
| `GET`   | `/users/me`             | Bearer | Get current user             |
| `PATCH` | `/users/me`             | Bearer | Update current user          |
| `GET`   | `/users/{id}`           | Bearer | Get public user profile      |

Architecture overview and ADRs in [ARCHITECTURE.md](./ARCHITECTURE.md) and [docs/adr/](./docs/adr/README.md).

## Development

```bash
# Install the ktlint pre-commit hook (once per clone)
./gradlew addKtlintCheckGitPreCommitHook

# Run tests
./gradlew test
```

## CI/CD

| Workflow      | Trigger               | Description                                |
|---------------|-----------------------|--------------------------------------------|
| `ci.yml`      | Pull request          | Lint + tests                               |
| `codeql.yml`  | Pull request + weekly | Static security analysis                   |
| `publish.yml` | Manual / tags         | Docker build, push to GHCR, GitHub release |

> Branch protection rules and secrets are not copied when using this template — configure them after running `init.sh`.
