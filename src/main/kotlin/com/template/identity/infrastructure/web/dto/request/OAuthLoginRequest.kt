package com.template.identity.infrastructure.web.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

data class OAuthLoginRequest(
    @field:NotBlank
    @field:Schema(description = "OAuth provider name", example = "GOOGLE", allowableValues = ["GOOGLE"])
    val provider: String,

    @field:NotBlank
    @field:Schema(description = "ID token returned by the OAuth provider after consent")
    val idToken: String,
)
