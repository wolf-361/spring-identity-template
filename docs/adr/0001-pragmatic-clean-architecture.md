# ADR-0001: Pragmatic Clean Architecture with Use Cases

## Status

Accepted — 2026-04

## Context

This template needs an architecture that is:

- **Rigorous enough** to scale across multiple services and teams without turning into a mud-ball.
- **Light enough** to avoid the over-engineering trap where every feature takes three layers of ceremony.
- **Recognizable** to any Spring Kotlin developer who joins the project.

I had previously built services using NestJS with a standard layered structure (`controller → service → repository`). That worked, but service classes grew into 400-line objects holding 12 loosely related methods. Testing required mocking half the codebase.

I wanted the benefits of Clean Architecture (inverted dependencies, testable core) without the ceremony of textbook Hexagonal (Ports & Adapters terminology, mandatory separation of JPA entities from domain entities, domain interfaces on everything).

## Decision

I adopted a **three-layer Clean Architecture**:

- `domain/` — business entities and invariants
- `application/` — use cases + interfaces for anything the use case doesn't own
- `infrastructure/` — all adapters (controllers, persistence, external clients)

With these specific choices:

1. **Use cases, not services.** One class per action. Public method is always `execute(command): result`.
2. **Lightweight interface pattern.** Interfaces live in `application/`, implementations in `infrastructure/`. I don't use "Port" / "Adapter" naming — the folder location conveys the role.
3. **Flat structure inside each layer** for this specific service. This template is single-domain (identity); feature-first organization adds nothing.
4. **Feature-first is reserved** for multi-domain services where one layer would contain unrelated concerns. (The main product app using this template will likely be feature-first; that's a choice made per-project, not at the template level.)

## Consequences

### Positive

- **Testing is trivial.** Each use case has 2–4 dependencies. Mock them, test one method.
- **Responsibility is obvious.** If you're adding an endpoint, you add a use case. No guessing where logic belongs.
- **Use cases compose.** A complex flow can orchestrate multiple use cases in a controller or a higher-level use case, without fat-service cross-coupling.
- **Inverted dependencies.** Domain and application don't import Spring (beyond the narrow JPA tradeoff in ADR-0002). Swapping Postgres for DynamoDB, or Spring for Ktor, only touches `infrastructure/`.

### Negative / Tradeoffs

- **More files.** ~13 use cases for identity alone vs 2 service classes in a traditional layout. I consider this a feature — file count maps to endpoint count, which is a legitimate measure of app size.
- **Boilerplate for small actions.** A one-line use case still needs a `Command`, `Result`, and `execute()` method. I accept this — consistency beats the occasional shortcut.
- **Cross-use-case reuse** (e.g., token issuing shared between `LoginUseCase` and `OAuthLoginUseCase`) needs a shared private service. I handle this with application-layer services injected into both — not with a use case calling another use case.

## Alternatives Considered

### Traditional layered architecture (Controller / Service / Repository)

Rejected. Previous experience showed service classes grow into unmaintainable god objects. Test setup becomes painful. Forces developers to mentally parse which methods of a service belong together.

### Full Hexagonal / Ports & Adapters with strict port/adapter naming

Rejected. Adds ceremony without proportional benefit for an identity service of this size. Renaming interfaces `UserRepositoryPort` and implementations `UserRepositoryAdapter` is vocabulary overhead, not architectural clarity. The folder location (`application/` vs `infrastructure/`) already conveys the role.

### Feature-first at the top level

Rejected for *this* service. Feature-first shines when a service has multiple unrelated domains (e.g., the Planific main app has agenda, courses, assignments — each deserves its own feature folder). An identity service has one concern. Splitting it into `features/auth/` and `features/user/` would be artificial — they share entities and use cases extensively.
