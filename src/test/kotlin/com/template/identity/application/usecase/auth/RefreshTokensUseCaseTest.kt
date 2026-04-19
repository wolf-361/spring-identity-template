package com.template.identity.application.usecase.auth

import com.template.identity.application.command.RefreshTokensCommand
import com.template.identity.application.exception.ApplicationException
import com.template.identity.application.repository.RefreshTokenRepository
import com.template.identity.application.service.TokenPairIssuer
import com.template.identity.buildAuthResult
import com.template.identity.buildRefreshToken
import com.template.identity.buildUser
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.time.Instant

class RefreshTokensUseCaseTest {

    private val refreshTokenRepository: RefreshTokenRepository = mockk()
    private val tokenPairIssuer: TokenPairIssuer = mockk()
    private val useCase = RefreshTokensUseCase(refreshTokenRepository, tokenPairIssuer)

    @Test
    fun `should revoke old token and issue new token pair`() {
        // Arrange
        val user = buildUser()
        val token = buildRefreshToken(user = user)
        every { refreshTokenRepository.findByTokenHash(any()) } returns token
        every { refreshTokenRepository.revoke(token.id!!) } just runs
        every { tokenPairIssuer.issue(user, token.familyId) } returns buildAuthResult(user)

        // Act
        val result = useCase.execute(RefreshTokensCommand(rawRefreshToken = "raw-token"))

        // Assert
        result.user.id shouldBe user.id
        verify(exactly = 1) { refreshTokenRepository.revoke(token.id!!) }
        verify(exactly = 1) { tokenPairIssuer.issue(user, token.familyId) }
    }

    @Test
    fun `should throw RefreshTokenInvalid when token hash is not found`() {
        // Arrange
        every { refreshTokenRepository.findByTokenHash(any()) } returns null

        // Act & Assert
        shouldThrow<ApplicationException.RefreshTokenInvalid> {
            useCase.execute(RefreshTokensCommand(rawRefreshToken = "unknown-token"))
        }
    }

    @Test
    fun `should revoke entire family and throw RefreshTokenRevoked when revoked token is reused`() {
        // Arrange
        val user = buildUser()
        val revokedToken = buildRefreshToken(user = user, revokedAt = Instant.now().minusSeconds(60))
        every { refreshTokenRepository.findByTokenHash(any()) } returns revokedToken
        every { refreshTokenRepository.revokeAllByFamilyId(revokedToken.familyId) } just runs

        // Act & Assert
        shouldThrow<ApplicationException.RefreshTokenRevoked> {
            useCase.execute(RefreshTokensCommand(rawRefreshToken = "reused-token"))
        }
        verify(exactly = 1) { refreshTokenRepository.revokeAllByFamilyId(revokedToken.familyId) }
    }

    @Test
    fun `should throw RefreshTokenExpired when token TTL has passed`() {
        // Arrange
        val expiredToken = buildRefreshToken(
            user = buildUser(),
            expiresAt = Instant.now().minusSeconds(60),
        )
        every { refreshTokenRepository.findByTokenHash(any()) } returns expiredToken

        // Act & Assert
        shouldThrow<ApplicationException.RefreshTokenExpired> {
            useCase.execute(RefreshTokensCommand(rawRefreshToken = "expired-token"))
        }
        verify(exactly = 0) { refreshTokenRepository.revoke(any()) }
    }
}
