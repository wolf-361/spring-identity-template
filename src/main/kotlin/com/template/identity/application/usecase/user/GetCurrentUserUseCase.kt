package com.template.identity.application.usecase.user

import com.template.identity.application.command.GetCurrentUserCommand
import com.template.identity.application.result.UserResult
import com.template.identity.application.exception.ApplicationException
import com.template.identity.application.repository.UserRepository
import org.springframework.stereotype.Service

/**
 * Returns the full private profile of the authenticated user.
 *
 * @throws ApplicationException.UserNotFound if no user exists for [GetCurrentUserCommand.userId].
 */
@Service
class GetCurrentUserUseCase(
    private val userRepository: UserRepository,
) {
    fun execute(command: GetCurrentUserCommand): UserResult {
        val user = userRepository.findById(command.userId)
            ?: throw ApplicationException.UserNotFound()
        return UserResult.from(user)
    }
}
