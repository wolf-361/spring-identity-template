package com.template.identity.application.usecase.user

import com.template.identity.application.command.UpdateCurrentUserCommand
import com.template.identity.application.exception.ApplicationException
import com.template.identity.application.repository.UserRepository
import com.template.identity.application.result.UserResult
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Updates the authenticated user's mutable profile fields.
 *
 * Only fields present in the command (non-null) are applied — absent fields are left unchanged.
 * Email changes are checked for uniqueness before being applied.
 *
 * @throws ApplicationException.UserNotFound if no user exists for [UpdateCurrentUserCommand.userId].
 * @throws ApplicationException.EmailAlreadyExists if the requested email is taken by another account.
 */
@Service
class UpdateCurrentUserUseCase(
    private val userRepository: UserRepository
) {
    @Transactional
    fun execute(command: UpdateCurrentUserCommand): UserResult {
        val user =
            userRepository.findById(command.userId)
                ?: throw ApplicationException.UserNotFound()

        if (command.email != null && command.email != user.email) {
            if (userRepository.existsByEmail(command.email)) {
                throw ApplicationException.EmailAlreadyExists()
            }
            user.email = command.email
        }

        command.firstName?.let { user.firstName = it }
        command.lastName?.let { user.lastName = it }

        return UserResult.from(userRepository.save(user))
    }
}