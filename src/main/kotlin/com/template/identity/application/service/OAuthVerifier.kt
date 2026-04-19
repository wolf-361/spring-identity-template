package com.template.identity.application.service

import com.template.identity.application.result.OAuthUserInfo
import com.template.identity.domain.model.OAuthProvider

/**
 * Verifies an OAuth ID token and returns the authenticated user's identity.
 * Implemented by [com.template.identity.infrastructure.external.oauth.OAuthProviderRegistry],
 * which dispatches to provider-specific verifiers.
 */
interface OAuthVerifier {
    /**
     * Verifies [idToken] with the given [provider] and returns the user's identity.
     *
     * @throws com.template.identity.application.exception.ApplicationException.OAuthProviderNotSupported
     *   if no verifier is registered for [provider].
     * @throws RuntimeException if the token is invalid, expired, or fails provider-side verification.
     */
    fun verify(
        provider: OAuthProvider,
        idToken: String
    ): OAuthUserInfo
}