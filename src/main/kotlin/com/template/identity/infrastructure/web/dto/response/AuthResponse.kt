package com.template.identity.infrastructure.web.dto.response

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Token pair and user profile returned after successful authentication")
data class AuthResponse(
    @field:Schema(description = "Short-lived JWT access token (15 min)")
    val accessToken: String,
    @field:Schema(description = "Long-lived opaque refresh token (30 days) — store securely, rotate on each use")
    val refreshToken: String,
    val user: UserResponse
)