package com.template.identity.infrastructure.external.oauth

import com.template.identity.application.exception.ApplicationException
import com.template.identity.application.result.OAuthUserInfo
import com.template.identity.application.service.OAuthVerifier
import com.template.identity.domain.model.OAuthProvider
import org.springframework.stereotype.Service

@Service
class OAuthProviderRegistry(
    verifiers: List<ProviderVerifier>
) : OAuthVerifier {
    private val registry: Map<OAuthProvider, ProviderVerifier> = verifiers.associateBy { it.provider }

    override fun verify(
        provider: OAuthProvider,
        idToken: String
    ): OAuthUserInfo =
        registry[provider]?.verify(idToken)
            ?: throw ApplicationException.OAuthProviderNotSupported()
}