package com.template.identity.application.usecase.auth

import com.template.identity.application.command.LoginCommand
import com.template.identity.application.exception.ApplicationException
import com.template.identity.application.repository.UserRepository
import com.template.identity.application.result.AuthenticationResult
import com.template.identity.application.service.PasswordEncoder
import com.template.identity.application.service.TokenPairIssuer
import org.springframework.stereotype.Service

/**
 * Authenticates a user with email and password and issues a new token pair.
 *
 * All failure cases (user not found, wrong password, OAuth-only account, inactive account)
 * throw [ApplicationException.InvalidCredentials] to prevent user enumeration (ADR-0004).
 */
@Service
class LoginUseCase(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val tokenPairIssuer: TokenPairIssuer
) {
    fun execute(command: LoginCommand): AuthenticationResult {
        val user =
            userRepository.findByEmail(command.email)
                ?: throw ApplicationException.InvalidCredentials()

        if (!user.isActive) throw ApplicationException.InvalidCredentials()
        if (!user.hasPassword()) throw ApplicationException.InvalidCredentials()
        if (!passwordEncoder.matches(command.password, user.password!!)) {
            throw ApplicationException.InvalidCredentials()
        }

        return tokenPairIssuer.issue(user)
    }
}