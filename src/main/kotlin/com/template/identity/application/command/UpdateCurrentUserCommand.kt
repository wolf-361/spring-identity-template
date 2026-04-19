package com.template.identity.application.command

import java.util.UUID

data class UpdateCurrentUserCommand(
    val userId: UUID,
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
)
