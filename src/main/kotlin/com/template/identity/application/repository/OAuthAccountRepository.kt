package com.template.identity.application.repository

import com.template.identity.domain.model.OAuthAccount
import com.template.identity.domain.model.OAuthProvider
import com.template.identity.domain.model.User

/** Persistence contract for [OAuthAccount] entities. */
interface OAuthAccountRepository {

    /**
     * Returns the OAuth account matching the given [provider] and external [providerUserId],
     * or null if no account is linked yet.
     */
    fun findByProviderAndProviderUserId(provider: OAuthProvider, providerUserId: String): OAuthAccount?

    /** Returns the OAuth account for [user] with [provider], or null if not linked. */
    fun findByUserAndProvider(user: User, provider: OAuthProvider): OAuthAccount?

    /**
     * Returns the total number of OAuth accounts linked to [user].
     * Used to enforce the last-auth-method invariant before unlinking.
     */
    fun countByUser(user: User): Int

    /** Persists [account] and returns the saved instance. */
    fun save(account: OAuthAccount): OAuthAccount

    /** Deletes [account], effectively unlinking the OAuth provider from the user. */
    fun delete(account: OAuthAccount)
}
