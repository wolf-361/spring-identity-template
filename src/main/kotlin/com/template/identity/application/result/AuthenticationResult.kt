package com.template.identity.application.result

import com.template.identity.domain.model.User

data class AuthenticationResult(
    val accessToken: String,
    val refreshToken: String,
    val user: User,
)
