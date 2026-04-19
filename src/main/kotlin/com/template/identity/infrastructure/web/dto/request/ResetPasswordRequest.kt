package com.template.identity.infrastructure.web.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class ResetPasswordRequest(
    @field:NotBlank
    @field:Schema(description = "Raw token from the password reset link query parameter")
    val token: String,
    @field:NotBlank @field:Size(min = 8, max = 128)
    @field:Schema(description = "New password — minimum 8 characters", example = "NewPassword456!")
    val newPassword: String
)