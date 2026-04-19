package com.template.identity.application.usecase.auth

import com.template.identity.application.command.LogoutCommand
import com.template.identity.application.repository.RefreshTokenRepository
import com.template.identity.buildRefreshToken
import com.template.identity.buildUser
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test

class LogoutUseCaseTest {

    private val refreshTokenRepository: RefreshTokenRepository = mockk()
    private val useCase = LogoutUseCase(refreshTokenRepository)

    @Test
    fun `should revoke the refresh token on logout`() {
        // Arrange
        val token = buildRefreshToken(user = buildUser())
        every { refreshTokenRepository.findByTokenHash(any()) } returns token
        every { refreshTokenRepository.revoke(token.id!!) } just runs

        // Act
        useCase.execute(LogoutCommand(rawRefreshToken = "raw-token"))

        // Assert
        verify(exactly = 1) { refreshTokenRepository.revoke(token.id!!) }
    }

    @Test
    fun `should do nothing when refresh token is not found`() {
        // Arrange
        every { refreshTokenRepository.findByTokenHash(any()) } returns null

        // Act
        useCase.execute(LogoutCommand(rawRefreshToken = "unknown-token"))

        // Assert
        verify(exactly = 0) { refreshTokenRepository.revoke(any()) }
    }
}
