# ADR-0003: Opaque Refresh Tokens with Rotation

## Status

Accepted — 2026-04

## Context

Authentication needs both short-lived access tokens (what the client sends on every request) and a longer-lived mechanism to obtain new access tokens without forcing the user to log in again.

Two common options for the refresh mechanism:

1. **Signed JWT refresh tokens** — stateless, verified cryptographically, no database lookup.
2. **Opaque refresh tokens** — random strings stored in a database, validated by lookup. (Can't be decrypted for data like a JWT)

And two common options for token lifecycle:

1. **Static refresh token** — the same token is used repeatedly until it expires.
2. **Rotated refresh token** — each `/refresh` call issues a new token and revokes the old one.

These choices have significant security implications and must be made together.

## Decision

- **Refresh tokens are opaque**, 256-bit random strings, stored hashed (SHA-256) in a `refresh_tokens` table.
- **Access tokens are short-lived JWTs** — 15 minutes.
- **Refresh tokens are long-lived** — 30 days.
- **Rotation is enabled.** Every `/auth/refresh` call issues a new refresh token and revokes the one that was used.
- **Reuse detection.** If a revoked token is ever presented, the entire token family for that user is revoked, forcing re-authentication.

## Consequences

### Positive

- **Revocable by design.** A compromised or stolen refresh token can be invalidated server-side (e.g., on logout, password change, suspicious activity). JWT refresh tokens cannot be revoked without a blocklist — at which point they aren't stateless anymore.
- **Theft detection.** With rotation + reuse detection, if an attacker steals a refresh token and uses it, the legitimate user's next refresh fails. I log the reuse attempt, revoke the token family, and the user is forced to log in again. Without rotation, the theft is invisible.
- **Hashed storage.** Even if the `refresh_tokens` table leaks, the raw tokens aren't there. Only hashes, unusable without the original.
- **Short access token TTL limits exposure.** A stolen access token is useful for at most 15 minutes. Aligns with OAuth 2.0 best practices (RFC 8725, BCP 205).

### Negative / Tradeoffs

- **Stateful.** Every refresh hits the database. For identity-scale services, this is negligible (refreshes are infrequent compared to regular API calls). For extreme scale, a cache layer would front the table, but I don't need that here.
- **More DB writes** because of rotation. Negligible.
- **Slight complexity in the refresh flow.** I must atomically revoke the old token and issue the new one. A small transactional concern, nothing architectural.

## Alternatives Considered

### Signed JWT refresh tokens (stateless)

Rejected. The inability to revoke them is a deal-breaker for any service that cares about security incidents. The typical workaround — maintaining a revoked-token blocklist — makes them stateful anyway, at which point they've lost their only advantage and gained complexity.

### Opaque tokens without rotation

Rejected. Static refresh tokens make token theft invisible. An attacker who obtains a refresh token can use it repeatedly for 30 days without the legitimate user noticing. Rotation with reuse detection is the standard mitigation and costs nothing meaningful to implement.

### Long-lived access tokens, no refresh tokens

Rejected. Long TTL access tokens are dangerous — they can't be revoked, and if stolen, give the attacker full API access for their entire lifetime. The short-access-token + refresh-token pattern exists specifically to minimize this blast radius.

## References

- OAuth 2.0 Security Best Current Practice
