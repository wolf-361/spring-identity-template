package com.template.identity.application.repository

import com.template.identity.domain.model.RefreshToken
import java.util.UUID

/** Persistence contract for [RefreshToken] entities. */
interface RefreshTokenRepository {

    /**
     * Returns the refresh token whose stored SHA-256 hash matches [tokenHash], or null.
     * Lookup is always by hash — raw tokens are never stored.
     */
    fun findByTokenHash(tokenHash: String): RefreshToken?

    /** Persists [token] and returns the saved instance. */
    fun save(token: RefreshToken): RefreshToken

    /**
     * Marks the token with [tokenId] as revoked by setting its [RefreshToken.revokedAt] timestamp.
     * Called on every successful refresh to rotate the old token out.
     */
    fun revoke(tokenId: UUID)

    /**
     * Revokes every token in the rotation chain identified by [familyId].
     * Called when reuse of a previously revoked token is detected, forcing re-authentication.
     */
    fun revokeAllByFamilyId(familyId: UUID)

    /** Deletes all tokens whose [RefreshToken.expiresAt] is in the past. Called by the cleanup scheduler. */
    fun deleteExpired()
}
