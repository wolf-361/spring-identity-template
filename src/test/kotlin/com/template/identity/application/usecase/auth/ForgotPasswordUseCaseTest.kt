package com.template.identity.application.usecase.auth

import com.template.identity.application.command.ForgotPasswordCommand
import com.template.identity.application.repository.PasswordResetTokenRepository
import com.template.identity.application.repository.UserRepository
import com.template.identity.application.service.EmailSender
import com.template.identity.buildUser
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test

class ForgotPasswordUseCaseTest {
    private val userRepository: UserRepository = mockk()
    private val passwordResetTokenRepository: PasswordResetTokenRepository = mockk(relaxUnitFun = true)
    private val emailSender: EmailSender = mockk()
    private val useCase = ForgotPasswordUseCase(userRepository, passwordResetTokenRepository, emailSender)

    private val command =
        ForgotPasswordCommand(
            email = "user@example.com",
            frontendUrl = "https://app.example.com"
        )

    @Test
    fun `should delete existing token, save new token, and send reset email`() {
        // Arrange
        val user = buildUser(email = command.email)
        every { userRepository.findByEmail(command.email) } returns user
        every { passwordResetTokenRepository.save(any()) } answers { firstArg() }
        val resetLinkSlot = slot<String>()
        every {
            emailSender.sendPasswordResetEmail(user.email, user.firstName, capture(resetLinkSlot))
        } just runs

        // Act
        useCase.execute(command)

        // Assert
        verify(exactly = 1) { passwordResetTokenRepository.deleteByUser(user) }
        verify(exactly = 1) { passwordResetTokenRepository.save(any()) }
        verify(exactly = 1) { emailSender.sendPasswordResetEmail(user.email, user.firstName, any()) }
        assert(resetLinkSlot.captured.startsWith("https://app.example.com/reset-password?token="))
    }

    @Test
    fun `should do nothing when email is not registered`() {
        // Arrange
        every { userRepository.findByEmail(command.email) } returns null

        // Act
        useCase.execute(command)

        // Assert
        verify(exactly = 0) { passwordResetTokenRepository.save(any()) }
        verify(exactly = 0) { emailSender.sendPasswordResetEmail(any(), any(), any()) }
    }
}