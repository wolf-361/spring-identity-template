package com.template.identity.infrastructure.external.oauth

import com.template.identity.application.result.OAuthUserInfo
import com.template.identity.domain.model.OAuthProvider

interface ProviderVerifier {
    val provider: OAuthProvider

    fun verify(idToken: String): OAuthUserInfo
}