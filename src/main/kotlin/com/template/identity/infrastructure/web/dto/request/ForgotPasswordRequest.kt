package com.template.identity.infrastructure.web.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class ForgotPasswordRequest(
    @field:Email @field:NotBlank
    @field:Schema(description = "Email address of the account to reset", example = "user@example.com")
    val email: String,
)
