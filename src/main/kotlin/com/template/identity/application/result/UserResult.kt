package com.template.identity.application.result

import com.template.identity.domain.model.User
import java.time.Instant
import java.util.UUID

data class UserResult(
    val id: UUID,
    val email: String,
    val firstName: String,
    val lastName: String,
    val isActive: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    companion object {
        fun from(user: User) = UserResult(
            id = user.id!!,
            email = user.email,
            firstName = user.firstName,
            lastName = user.lastName,
            isActive = user.isActive,
            createdAt = user.createdAt,
            updatedAt = user.updatedAt,
        )
    }
}
