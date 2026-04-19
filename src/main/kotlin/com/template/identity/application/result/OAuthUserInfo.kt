package com.template.identity.application.result

data class OAuthUserInfo(
    val providerUserId: String,
    val email: String,
    val firstName: String,
    val lastName: String
)