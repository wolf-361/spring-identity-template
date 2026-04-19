package com.template.identity.infrastructure.web.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class RegisterRequest(
    @field:Email @field:NotBlank
    @field:Schema(description = "Email address — must be unique", example = "user@example.com")
    val email: String,

    @field:NotBlank @field:Size(min = 8, max = 128)
    @field:Schema(description = "Password — minimum 8 characters", example = "MyPassword123!")
    val password: String,

    @field:NotBlank @field:Size(max = 100)
    @field:Schema(description = "First name", example = "Jane")
    val firstName: String,

    @field:NotBlank @field:Size(max = 100)
    @field:Schema(description = "Last name", example = "Doe")
    val lastName: String,
)
