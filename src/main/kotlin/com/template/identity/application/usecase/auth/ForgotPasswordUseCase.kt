package com.template.identity.application.usecase.auth

import com.template.identity.application.command.ForgotPasswordCommand
import com.template.identity.application.generateSecureToken
import com.template.identity.application.repository.PasswordResetTokenRepository
import com.template.identity.application.repository.UserRepository
import com.template.identity.application.service.EmailSender
import com.template.identity.application.sha256
import com.template.identity.domain.model.PasswordResetToken
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

/**
 * Initiates the password reset flow by generating a hashed token and sending a magic link.
 *
 * Intentionally silent when the email is not found to prevent user enumeration (ADR-0004).
 * Any existing reset token for the user is deleted before issuing a new one —
 * only one active token per user at a time.
 */
@Service
class ForgotPasswordUseCase(
    private val userRepository: UserRepository,
    private val passwordResetTokenRepository: PasswordResetTokenRepository,
    private val emailSender: EmailSender,
) {
    @Transactional
    fun execute(command: ForgotPasswordCommand) {
        val user = userRepository.findByEmail(command.email) ?: return

        passwordResetTokenRepository.deleteByUser(user)

        val rawToken = generateSecureToken()
        passwordResetTokenRepository.save(
            PasswordResetToken(
                user = user,
                tokenHash = sha256(rawToken),
                expiresAt = Instant.now().plusSeconds(3600),
            )
        )

        val resetLink = "${command.frontendUrl}/reset-password?token=$rawToken"
        emailSender.sendPasswordResetEmail(user.email, user.firstName, resetLink)
    }
}
