package com.template.identity.application.exception

/**
 * Sealed hierarchy of business-rule failures raised by use cases.
 *
 * Mapped to generic external responses by [com.template.identity.infrastructure.web.exception.GlobalExceptionHandler].
 * Clients never see internal exception names or messages — only a stable [code] and a safe [message].
 */
sealed class ApplicationException(message: String) : RuntimeException(message) {

    // ── Auth ──────────────────────────────────────────────────────────────────

    /**
     * Raised when login fails for any reason: email not found, wrong password, or inactive account.
     * All cases collapse into this single type intentionally — callers cannot distinguish them,
     * which prevents user enumeration attacks (ADR-0004).
     */
    class InvalidCredentials : ApplicationException("Invalid credentials")

    /**
     * Raised when an inactive account attempts to authenticate.
     * Maps to [InvalidCredentials] externally so clients cannot detect account state.
     */
    class UserInactive : ApplicationException("Account is inactive")

    /**
     * Raised during registration when the requested email is already in use.
     * Maps to a generic "Unable to create account" response externally.
     */
    class EmailAlreadyExists : ApplicationException("Email already in use")

    // ── Refresh tokens ────────────────────────────────────────────────────────

    /** Raised when the presented refresh token hash is not found in the database. */
    class RefreshTokenInvalid : ApplicationException("Refresh token not found or invalid")

    /** Raised when the refresh token exists but its TTL has passed. */
    class RefreshTokenExpired : ApplicationException("Refresh token expired")

    /**
     * Raised when a previously revoked token is presented again (reuse detection).
     * Triggers family-wide revocation before throwing, forcing full re-authentication.
     */
    class RefreshTokenRevoked : ApplicationException("Revoked refresh token reuse detected")

    // ── Password reset ────────────────────────────────────────────────────────

    /** Raised when the presented reset token hash is not found in the database. */
    class PasswordResetTokenInvalid : ApplicationException("Password reset token not found or invalid")

    /** Raised when the reset token exists but its 1-hour TTL has passed. */
    class PasswordResetTokenExpired : ApplicationException("Password reset token expired")

    // ── User ──────────────────────────────────────────────────────────────────

    /** Raised when a user lookup by ID finds no matching record. */
    class UserNotFound : ApplicationException("User not found")

    // ── OAuth ─────────────────────────────────────────────────────────────────

    /** Raised when the requested [com.template.identity.domain.model.OAuthProvider] has no registered verifier. */
    class OAuthProviderNotSupported : ApplicationException("OAuth provider not supported")

    /** Raised when the verified OAuth account is already linked to a different user. */
    class OAuthAccountAlreadyLinked : ApplicationException("OAuth account is already linked to another user")
}
