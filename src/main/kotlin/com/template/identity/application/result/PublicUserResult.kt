package com.template.identity.application.result

import com.template.identity.domain.model.User
import java.util.UUID

data class PublicUserResult(
    val id: UUID,
    val firstName: String,
    val lastName: String
) {
    companion object {
        fun from(user: User) =
            PublicUserResult(
                id = user.id!!,
                firstName = user.firstName,
                lastName = user.lastName
            )
    }
}