package com.template.identity.application.usecase.auth

import com.template.identity.application.command.ResetPasswordCommand
import com.template.identity.application.exception.ApplicationException
import com.template.identity.application.repository.PasswordResetTokenRepository
import com.template.identity.application.service.PasswordEncoder
import com.template.identity.application.sha256
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Validates a password reset token and updates the user's password.
 *
 * The token is always deleted after use (success or expiry) — it is single-use.
 *
 * @throws ApplicationException.PasswordResetTokenInvalid if the token hash is not found.
 * @throws ApplicationException.PasswordResetTokenExpired if the 1-hour TTL has passed.
 */
@Service
class ResetPasswordUseCase(
    private val passwordResetTokenRepository: PasswordResetTokenRepository,
    private val passwordEncoder: PasswordEncoder
) {
    @Transactional
    fun execute(command: ResetPasswordCommand) {
        val tokenHash = sha256(command.rawToken)
        val resetToken =
            passwordResetTokenRepository.findByTokenHash(tokenHash)
                ?: throw ApplicationException.PasswordResetTokenInvalid()

        if (resetToken.isExpired()) {
            passwordResetTokenRepository.deleteByUser(resetToken.user)
            throw ApplicationException.PasswordResetTokenExpired()
        }

        resetToken.user.password = passwordEncoder.encode(command.newPassword)
        passwordResetTokenRepository.deleteByUser(resetToken.user)
    }
}