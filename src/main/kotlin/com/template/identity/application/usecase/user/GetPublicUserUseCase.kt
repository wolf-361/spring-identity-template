package com.template.identity.application.usecase.user

import com.template.identity.application.result.PublicUserResult
import com.template.identity.application.exception.ApplicationException
import com.template.identity.application.repository.UserRepository
import org.springframework.stereotype.Service
import java.util.UUID

/**
 * Returns the public profile of any user by ID.
 * Intended for endpoints accessible without authentication or by other services.
 *
 * @throws ApplicationException.UserNotFound if no user exists for [userId].
 */
@Service
class GetPublicUserUseCase(
    private val userRepository: UserRepository,
) {
    fun execute(userId: UUID): PublicUserResult {
        val user = userRepository.findById(userId)
            ?: throw ApplicationException.UserNotFound()
        return PublicUserResult.from(user)
    }
}
