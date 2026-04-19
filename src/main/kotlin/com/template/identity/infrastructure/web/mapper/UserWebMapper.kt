package com.template.identity.infrastructure.web.mapper

import com.template.identity.application.command.UpdateCurrentUserCommand
import com.template.identity.application.result.PublicUserResult
import com.template.identity.application.result.UserResult
import com.template.identity.infrastructure.web.dto.request.UpdateCurrentUserRequest
import com.template.identity.infrastructure.web.dto.response.PublicUserResponse
import com.template.identity.infrastructure.web.dto.response.UserResponse
import java.util.UUID

object UserWebMapper {
    fun toCommand(
        request: UpdateCurrentUserRequest,
        userId: UUID
    ) = UpdateCurrentUserCommand(
        userId = userId,
        email = request.email,
        firstName = request.firstName,
        lastName = request.lastName
    )

    fun toResponse(result: UserResult) =
        UserResponse(
            id = result.id,
            email = result.email,
            firstName = result.firstName,
            lastName = result.lastName,
            isActive = result.isActive,
            createdAt = result.createdAt,
            updatedAt = result.updatedAt
        )

    fun toPublicResponse(result: PublicUserResult) =
        PublicUserResponse(
            id = result.id,
            firstName = result.firstName,
            lastName = result.lastName
        )
}