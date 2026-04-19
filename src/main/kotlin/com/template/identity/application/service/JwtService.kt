package com.template.identity.application.service

import java.time.Duration
import java.util.UUID

/**
 * Issues and validates JWT access tokens, and exposes refresh token timing configuration.
 */
interface JwtService {

    /**
     * Generates a signed JWT access token for [userId].
     * TTL is determined by the `app.jwt.access-token-ttl` configuration property.
     */
    fun generateAccessToken(userId: UUID): String

    /**
     * Extracts the user ID from the `sub` claim of a JWT.
     * Throws if the token is malformed, tampered with, or the signature is invalid.
     */
    fun extractUserId(token: String): UUID

    /** Returns true if the token has a valid signature and has not expired. */
    fun isAccessTokenValid(token: String): Boolean

    /**
     * Returns the configured TTL for opaque refresh tokens.
     * Used by [TokenPairIssuer] to set [com.template.identity.domain.model.RefreshToken.expiresAt]
     * on newly issued tokens.
     */
    fun getRefreshTokenTtl(): Duration
}
