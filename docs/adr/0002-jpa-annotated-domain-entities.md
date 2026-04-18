# ADR-0002: JPA-Annotated Domain Entities

## Status

Accepted — 2026-04

## Context

Textbook Clean Architecture says the domain layer must not depend on frameworks. In practice, using JPA (Hibernate) means choosing between two approaches:

1. **Pure domain + separate persistence model** — `domain/User` is a plain Kotlin data class. A separate `infrastructure/persistence/UserJpaEntity` holds the JPA annotations. A mapper converts between them.
2. **Annotated domain entity** — `domain/User` carries `@Entity`, `@Column`, `@Id` directly. The same class is stored by JPA and used throughout the application.

Option 1 is the purist answer. It keeps the domain framework-free and allows, in theory, swapping persistence technology with zero domain changes.

Option 2 is what most production Spring Kotlin codebases actually do.

## Decision

I chose **Option 2 — JPA annotations on domain entities** for this template.

The domain model IS the persistence model. There is one `User` class, annotated with JPA, located in `domain/model/`.

## Consequences

### Positive

- **No mapping layer** between domain and persistence. One less set of classes, one less mapper, one less place for bugs to live.
- **Changes propagate cleanly.** Adding a field to `User` is one change, not three synchronized changes across domain, persistence entity, and mapper.
- **Readable code.** A new contributor sees `User` and understands both the business concept and how it's stored. No jumping between four files.
- **JPA features work naturally.** Lazy loading, relationships, cascades — all work without fighting a mapping layer.

### Negative / Tradeoffs

- **Domain depends on `jakarta.persistence`.** This is a real Clean Architecture violation. I accept it knowingly.
- **Swapping persistence technology is harder.** If I ever moved from JPA to JDBI or JOOQ, I'd refactor the domain package. In a realistic assessment of how often that happens in production services: essentially never.
- **Domain classes aren't usable outside a JPA context.** For example, deserializing a `User` from an event queue requires the JPA dependency on the classpath. For identity services, this doesn't come up.

### Explicit boundary

The JPA tradeoff applies **only to annotations on entity classes**. The domain layer still:

- Does **not** import `org.springframework.*`.
- Does **not** depend on Hibernate-specific types (`@DynamicUpdate`, `SessionFactory`, etc.).
- Does **not** import anything from `application/` or `infrastructure/`.

If I ever find myself reaching for a Spring annotation inside `domain/`, that's a signal I'm making a mistake and need to reconsider.

## Alternatives Considered

### Option 1 — Pure domain + separate JPA entity

Rejected as over-engineering for this service.

The reasoning: the cost of the mapping layer (more files, more duplication, more places to forget a field) is paid on every single feature. The benefit (theoretical persistence-technology swapping) is paid never.

I document this as a tradeoff, not a principle. A service with genuinely complex domain rules (e.g., a financial ledger) might legitimately revisit this choice.

### Kotlin value classes or DTOs as the "domain type"

Rejected. This adds a third kind of class — entity, DTO, "domain type" — and becomes confusing fast. The JPA entity is already a clean Kotlin data class with behavior; splitting it further doesn't help.
