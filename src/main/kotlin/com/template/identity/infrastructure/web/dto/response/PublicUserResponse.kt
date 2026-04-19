package com.template.identity.infrastructure.web.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

@Schema(description = "Public subset of a user profile — safe to expose to any caller")
data class PublicUserResponse(
    @field:Schema(description = "User unique identifier")
    val id: UUID,
    @field:Schema(description = "First name", example = "Jane")
    val firstName: String,
    @field:Schema(description = "Last name", example = "Doe")
    val lastName: String
)