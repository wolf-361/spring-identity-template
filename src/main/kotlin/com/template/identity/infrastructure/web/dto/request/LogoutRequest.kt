package com.template.identity.infrastructure.web.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

data class LogoutRequest(
    @field:NotBlank
    @field:Schema(description = "Refresh token to revoke")
    val refreshToken: String,
)
