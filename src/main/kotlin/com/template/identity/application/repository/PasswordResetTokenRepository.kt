package com.template.identity.application.repository

import com.template.identity.domain.model.PasswordResetToken
import com.template.identity.domain.model.User

/** Persistence contract for [PasswordResetToken] entities. */
interface PasswordResetTokenRepository {

    /**
     * Returns the token whose stored SHA-256 hash matches [tokenHash], or null.
     * Lookup is always by hash — the raw token sent in the reset link is never stored.
     */
    fun findByTokenHash(tokenHash: String): PasswordResetToken?

    /** Persists [token] and returns the saved instance. */
    fun save(token: PasswordResetToken): PasswordResetToken

    /**
     * Deletes any existing reset token for [user].
     * Called before issuing a new token to ensure only one active token per user at a time.
     */
    fun deleteByUser(user: User)

    /** Deletes all tokens whose [PasswordResetToken.expiresAt] is in the past. Called by the cleanup scheduler. */
    fun deleteExpired()
}
