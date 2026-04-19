package com.template.identity.infrastructure.web.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

data class LoginRequest(
    @field:NotBlank
    @field:Schema(description = "Registered email address", example = "user@example.com")
    val email: String,
    @field:NotBlank
    @field:Schema(description = "Account password", example = "MyPassword123!")
    val password: String
)