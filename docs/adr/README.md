# Architecture Decision Records

This folder contains the reasoning behind the major architectural choices in this template.

Each ADR documents:

- **Context** — what situation prompted the decision
- **Decision** — what I chose
- **Consequences** — what I gain and what I give up
- **Alternatives considered** — what I rejected and why

ADRs are **immutable once accepted**. If a decision is revisited, write a new ADR that supersedes the old one and update the status.

---

## Index

| #    | Title                                                                                  | Status   |
|:-----|:---------------------------------------------------------------------------------------|:---------|
| 0001 | [Pragmatic Clean Architecture with Use Cases](./0001-pragmatic-clean-architecture.md)  | Accepted |
| 0002 | [JPA-Annotated Domain Entities](./0002-jpa-annotated-domain-entities.md)               | Accepted |
| 0003 | [Opaque Refresh Tokens with Rotation](./0003-opaque-refresh-tokens-with-rotation.md)   | Accepted |
| 0004 | [Generic External Error Messages](./0004-generic-external-error-messages.md)           | Accepted |
| 0005 | [Observability via Spring Boot Actuator](./0005-observability-via-actuator.md)         | Accepted |
| 0006 | [Logging Built In-Place, Extracted Later](./0006-logging-in-place-then-extract.md)     | Accepted |

---

## Writing a New ADR

1. Copy the format of an existing ADR.
2. Number it sequentially (`NNNN-kebab-case-title.md`).
3. Open a PR — ADRs get reviewed like any other change.
4. Once merged, update this index.
