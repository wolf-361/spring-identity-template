package com.template.identity.application.command

import com.template.identity.domain.model.OAuthProvider

data class OAuthLoginCommand(
    val provider: OAuthProvider,
    val idToken: String,
)
