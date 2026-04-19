package com.template.identity.application.usecase.auth

import com.template.identity.application.command.ResetPasswordCommand
import com.template.identity.application.exception.ApplicationException
import com.template.identity.application.repository.PasswordResetTokenRepository
import com.template.identity.application.service.PasswordEncoder
import com.template.identity.buildPasswordResetToken
import com.template.identity.buildUser
import io.kotest.assertions.throwables.shouldThrow
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.time.Instant

class ResetPasswordUseCaseTest {

    private val passwordResetTokenRepository: PasswordResetTokenRepository = mockk(relaxUnitFun = true)
    private val passwordEncoder: PasswordEncoder = mockk()
    private val useCase = ResetPasswordUseCase(passwordResetTokenRepository, passwordEncoder)

    @Test
    fun `should update password and delete token on success`() {
        // Arrange
        val user = buildUser()
        val resetToken = buildPasswordResetToken(user = user)
        every { passwordResetTokenRepository.findByTokenHash(any()) } returns resetToken
        every { passwordEncoder.encode("new-password") } returns "new-hashed-password"

        // Act
        useCase.execute(ResetPasswordCommand(rawToken = "raw-token", newPassword = "new-password"))

        // Assert
        verify(exactly = 1) { passwordResetTokenRepository.deleteByUser(user) }
        assert(user.password == "new-hashed-password")
    }

    @Test
    fun `should throw PasswordResetTokenInvalid when token is not found`() {
        // Arrange
        every { passwordResetTokenRepository.findByTokenHash(any()) } returns null

        // Act & Assert
        shouldThrow<ApplicationException.PasswordResetTokenInvalid> {
            useCase.execute(ResetPasswordCommand(rawToken = "unknown-token", newPassword = "new-password"))
        }
    }

    @Test
    fun `should delete token and throw PasswordResetTokenExpired when token TTL has passed`() {
        // Arrange
        val user = buildUser()
        val expiredToken = buildPasswordResetToken(
            user = user,
            expiresAt = Instant.now().minusSeconds(60),
        )
        every { passwordResetTokenRepository.findByTokenHash(any()) } returns expiredToken

        // Act & Assert
        shouldThrow<ApplicationException.PasswordResetTokenExpired> {
            useCase.execute(ResetPasswordCommand(rawToken = "expired-token", newPassword = "new-password"))
        }
        verify(exactly = 1) { passwordResetTokenRepository.deleteByUser(user) }
        verify(exactly = 0) { passwordEncoder.encode(any()) }
    }
}
