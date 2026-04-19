package com.template.identity.infrastructure.web.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import java.util.UUID

@Schema(description = "Full user profile — only returned to the authenticated user")
data class UserResponse(
    @field:Schema(description = "User unique identifier")
    val id: UUID,

    @field:Schema(description = "Email address", example = "user@example.com")
    val email: String,

    @field:Schema(description = "First name", example = "Jane")
    val firstName: String,

    @field:Schema(description = "Last name", example = "Doe")
    val lastName: String,

    @field:Schema(description = "Whether the account is active and can authenticate")
    val isActive: Boolean,

    @field:Schema(description = "Account creation timestamp (ISO 8601)")
    val createdAt: Instant,

    @field:Schema(description = "Last update timestamp (ISO 8601)")
    val updatedAt: Instant,
)
