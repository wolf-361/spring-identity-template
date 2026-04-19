package com.template.identity.infrastructure.web.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Size

@Schema(description = "All fields are optional — only provided fields are updated")
data class UpdateCurrentUserRequest(
    @field:Email
    @field:Schema(description = "New email address — must be unique", example = "new@example.com")
    val email: String? = null,
    @field:Size(min = 1, max = 100)
    @field:Schema(description = "New first name", example = "Jane")
    val firstName: String? = null,
    @field:Size(min = 1, max = 100)
    @field:Schema(description = "New last name", example = "Doe")
    val lastName: String? = null
)