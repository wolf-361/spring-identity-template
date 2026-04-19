package com.template.identity.application.usecase.auth

import com.template.identity.application.command.RegisterUserCommand
import com.template.identity.application.exception.ApplicationException
import com.template.identity.application.repository.UserRepository
import com.template.identity.application.result.AuthenticationResult
import com.template.identity.application.service.PasswordEncoder
import com.template.identity.application.service.TokenPairIssuer
import com.template.identity.domain.model.User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Creates a new user account with email/password and immediately issues a token pair.
 *
 * @throws ApplicationException.EmailAlreadyExists if the email is already registered.
 */
@Service
class RegisterUserUseCase(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val tokenPairIssuer: TokenPairIssuer
) {
    @Transactional
    fun execute(command: RegisterUserCommand): AuthenticationResult {
        if (userRepository.existsByEmail(command.email)) {
            throw ApplicationException.EmailAlreadyExists()
        }

        val user =
            User(
                email = command.email,
                password = passwordEncoder.encode(command.password),
                firstName = command.firstName,
                lastName = command.lastName
            )
        val savedUser = userRepository.save(user)

        return tokenPairIssuer.issue(savedUser)
    }
}