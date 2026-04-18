# ADR-0004: Generic External Error Messages

## Status

Accepted — 2026-04

## Context

Error messages returned to clients are a security surface. Specific messages leak information that attackers can weaponize:

- `"User not found"` vs `"Incorrect password"` tells an attacker which emails are registered — enabling user enumeration.
- `"Account is locked"` vs `"Account is inactive"` reveals internal account state.
- `"Session expired"` vs `"Token signature invalid"` helps an attacker distinguish between their token being aged out vs detected as forged.

At the same time, *developers* need detailed error information to debug production issues — vague "something went wrong" logs are useless during incidents.

## Decision

I implemented a **two-tier error system**:

- **Internally**, exceptions are specific. The `ApplicationException` sealed hierarchy distinguishes between `UserNotFound`, `InvalidPassword`, `UserInactive`, `RefreshTokenExpired`, etc. Logs contain the full detail, stack trace, and correlation ID.
- **Externally**, error responses use:
    - A **generic message** that does not leak account state or internal logic.
    - A **stable error code** that clients can branch on for UX purposes, without revealing state.

Mapping is centralized in `GlobalExceptionHandler`.

### Error response shape

~~~json
{
  "code": "INVALID_CREDENTIALS",
  "message": "Invalid credentials",
  "timestamp": "2026-04-18T14:30:00Z",
  "correlationId": "abc-123"
}
~~~

### Specific mappings

| Internal exception                     | External message                        | External code        |
|:---------------------------------------|:----------------------------------------|:---------------------|
| `UserNotFound` (during login)          | `"Invalid credentials"`                 | `INVALID_CREDENTIALS`|
| `InvalidPassword`                      | `"Invalid credentials"`                 | `INVALID_CREDENTIALS`|
| `UserInactive`                         | `"Invalid credentials"`                 | `INVALID_CREDENTIALS`|
| `EmailAlreadyExists` (during register) | `"Unable to create account"`            | `REGISTRATION_FAILED`|
| `RefreshTokenExpired`                  | `"Session expired, please login again"` | `SESSION_EXPIRED`    |
| `RefreshTokenInvalid`                  | `"Session expired, please login again"` | `SESSION_EXPIRED`    |

All internal variants producing the same external output is **intentional** — the attacker cannot distinguish them, the user doesn't need to.

## Consequences

### Positive

- **No user enumeration** via login or password reset endpoints.
- **No account state leakage** through error messages.
- **Developers keep signal.** Logs are specific, include correlation IDs, and let support and on-call engineers find the real cause quickly.
- **Clients have stable branching.** The `code` field is part of the public contract. Clients can handle `SESSION_EXPIRED` distinctly from `INVALID_CREDENTIALS` without needing specific internal reasons.

### Negative / Tradeoffs

- **Slightly worse UX in legitimate cases.** A user who genuinely mistyped their email gets "Invalid credentials" instead of "No account exists with that email — would you like to sign up?". I accept this — the security-UX tradeoff favors security here. Offer a sign-up link on the login page itself as a mitigation.
- **Developers must resist the urge** to "just return a better message" when a support ticket comes in. The discipline is: fix the log, not the response.

### Log discipline (non-negotiable)

These values are **never** logged, under any circumstances:

- Plaintext passwords
- Access tokens, refresh tokens (raw or hashed)
- Password reset tokens
- OAuth ID tokens or authorization codes
- Full PII bodies (email is okay for correlation; do not log names + address + DOB together)

Correlation IDs and user IDs are always safe to log and **should** appear in every log line related to a request.

## Alternatives Considered

### Specific user-facing error messages

Rejected. Convenience for legitimate users is not worth the security risk. Attackers at scale do user enumeration — I don't help them.

### Only generic messages, no error codes

Rejected. Clients legitimately need to distinguish "your session expired, show the login screen" from "credentials are wrong, stay on the form". The `code` field provides this without leaking state to someone inspecting HTTP traffic without context.

### i18n error messages from the server

Deferred. The `code` approach makes i18n a client-side concern, which is simpler and gives the client full control over message wording per locale. If a future use case genuinely needs server-side i18n, that's a new ADR.
