# Spring Boot Kotlin Identity Service Template

A production-grade identity service template built with Kotlin, Spring Boot, and pragmatic Clean Architecture.

This repo is a **starting point** — clone, rename, adapt. It ships with JWT auth, refresh token rotation, OAuth (Google, extensible), password reset, and all the observability scaffolding you need to deploy to production.

![Status](https://img.shields.io/badge/Status-Template-blue) ![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-purple) ![Spring](https://img.shields.io/badge/Spring%20Boot-3.x-green)

---

## 🎯 What's Inside

- **Auth flows** — email/password, Google OAuth (extensible provider registry), password reset via email
- **JWT access tokens** (15min) + **opaque refresh tokens** stored in DB with rotation (30d)
- **Clean Architecture** — three layers (domain / application / infrastructure), one use case per action
- **Observability** — Actuator health/readiness/metrics, Prometheus-ready, JSON structured logs in prod
- **Migrations** — Flyway, versioned SQL files (`V1__create_users.sql`, `V2__...`)
- **OpenAPI** — Swagger UI at `/swagger-ui.html`

---

## 🛠 Stack

Kotlin 1.9+ · JDK 21 · Spring Boot 3 · PostgreSQL 18 · Flyway · jjwt 0.12 · MockK

---

## 🚀 Getting Started

### Prerequisites
- JDK 21
- Docker + Docker Compose (for local Postgres)

### Run locally

1. Copy the env file:
   ~~~bash
   cp .env.example .env
   ~~~

2. Start Postgres:
   ~~~bash
   docker compose up -d
   ~~~

3. Run the app:
   ~~~bash
   ./gradlew bootRun
   ~~~

4. Verify:
    - Swagger: http://localhost:8080/swagger-ui.html
    - Health:  http://localhost:8080/actuator/health

---

## 🧩 Using This Template

1. Click **"Use this template"** on GitHub to create a new repo.
2. Rename the package `com.template.identity` to your own (e.g., `dev.clicappart.identity`).
3. Update `application.yml` prefixes if needed.
4. Configure your secrets in `.env`.
5. Add your own use cases alongside the existing ones.
6. Adapt `OAuthProvider` if you're using providers other than Google.

---

## 📐 Documentation

- [**ARCHITECTURE.md**](./ARCHITECTURE.md) — daily reference: folder structure, layer rules, naming, patterns, rules
- [**docs/adr/**](./docs/adr/) — Architecture Decision Records explaining *why* each major choice was made

---

## 🔄 Adapting This Template

This is an opinionated template. The architecture rules in `ARCHITECTURE.md` are strict by design.

When a rule doesn't fit your project, **document the change as a new ADR** instead of silently diverging. This keeps the reasoning visible to whoever (including future you) picks up the repo later.
