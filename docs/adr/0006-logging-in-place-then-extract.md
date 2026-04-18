# ADR-0006: Logging Built In-Place, Extracted Later

## Status

Accepted — 2026-04

## Context

Structured logging, correlation IDs, request/response logging, and MDC population are concerns shared by every service in an organization. There are two paths to reuse:

1. **Build the logging setup as a reusable library/starter first.** Every new service imports it and gets the full pattern.
2. **Build it in place inside the first service.** Extract it to a library once it has proven itself in 2–3 services.

Option 1 sounds tempting — "write it once, use it everywhere." But building a library before you know what's actually reusable is speculative abstraction. The first service will discover constraints the library couldn't predict, and the library will need breaking changes early — which is the worst time to break a shared dependency.

## Decision

I build the full logging setup **inside this identity service template**, not as a separate library.

Once the template has been reused in 2–3 real services and the patterns have stabilized, I'll extract the shared pieces to a `template-logging-starter` library that new services can pull in as a single dependency.

### What I build now

- **Logback config** (`logback-spring.xml`)
    - Human-readable pattern layout for dev
    - JSON layout via `logstash-logback-encoder` for prod
    - Profile-based selection (`-Dspring.profiles.active=prod`)
- **`CorrelationIdFilter`** — assigns a UUID to every incoming request, sets it in MDC, returns it in the `X-Correlation-Id` response header.
- **`RequestLoggingFilter`** — logs request method, path, status code, and duration at INFO. Configurable via properties to log request/response bodies at DEBUG.
- **MDC conventions** — standardized keys across services:
    - `correlationId` — always populated
    - `userId` — populated after authentication
    - `path`, `method` — populated on every request
- **Log discipline rules** documented in `ARCHITECTURE.md` (see [ADR-0004](./0004-generic-external-error-messages.md)).

### What I build later (after 2–3 services)

- Extract `CorrelationIdFilter`, `RequestLoggingFilter`, MDC utilities, and Logback config to `template-logging-starter`.
- Publish as a Maven artifact.
- New services get all of it by adding a single dependency line.

## Consequences

### Positive

- **No speculative abstraction.** I build only what this service actually needs. When patterns emerge across multiple services, I'll know they're real patterns worth abstracting.
- **Iteration is fast.** Changing a filter in one service is a one-line diff. Changing it in a shared library requires versioning, coordinated releases across consumers, and migration.
- **Shape is right when I extract.** By the time I build the library, I'll have seen the patterns used in 3 services. I'll know which things differ per service (business-specific MDC keys, custom filters) and which don't (correlation ID, JSON format).

### Negative / Tradeoffs

- **Initial duplication.** Services 2 and 3 will copy logging setup from this template. I accept this — copy-paste is cheap, premature abstraction is expensive.
- **Risk of drift.** Services 2 and 3 may diverge from this service's logging setup before the library is extracted. Mitigation: explicitly document the logging pattern in `ARCHITECTURE.md`, review it in PRs, align at extraction time.
- **Extraction is work.** At some point I'll spend a day or two building the starter. Compared to rebuilding speculatively, then rebuilding again when the speculation was wrong, this is cheaper.

## Alternatives Considered

### Build `template-logging-starter` first

Rejected. Speculative abstraction — I'd be guessing what's reusable without data. Library authors who haven't used their own library tend to get the API wrong.

### Never extract; every service maintains its own logging config

Rejected. After 3+ services, the duplication becomes a real maintenance burden (patches must be applied everywhere; new observability requirements, like adding `requestId` in logs, touch every codebase). Extract at that point.

### Use a third-party logging starter

Considered. Some exist (e.g., `structlog4j`, Spring Cloud Sleuth for distributed tracing). I prefer my own thin layer because:

- I control the MDC keys and conventions.
- I don't inherit a dependency's release cadence, breaking changes, or abandonment risk.
- Spring Cloud Sleuth specifically is not needed yet (single-service, no distributed tracing); I can add OpenTelemetry later if that changes.
