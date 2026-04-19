package com.template.identity.infrastructure.web.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

data class RefreshTokenRequest(
    @field:NotBlank
    @field:Schema(description = "Opaque refresh token previously issued by this service")
    val refreshToken: String
)