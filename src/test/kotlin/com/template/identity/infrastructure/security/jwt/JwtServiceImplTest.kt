package com.template.identity.infrastructure.security.jwt

import com.template.identity.infrastructure.config.JwtProperties
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.Duration
import java.util.UUID

class JwtServiceImplTest {
    private val validProperties =
        JwtProperties(
            secret = "test-secret-key-that-is-at-least-32-characters-long-for-hmac",
            accessTokenTtl = Duration.ofMinutes(15),
            refreshTokenTtl = Duration.ofDays(30)
        )

    private val service = JwtServiceImpl(validProperties)

    @Test
    fun `should generate a non-blank access token`() {
        // Arrange
        val userId = UUID.randomUUID()

        // Act
        val token = service.generateAccessToken(userId)

        // Assert
        token.isNotBlank().shouldBeTrue()
    }

    @Test
    fun `should extract the correct user id from a generated token`() {
        // Arrange
        val userId = UUID.randomUUID()
        val token = service.generateAccessToken(userId)

        // Act
        val extracted = service.extractUserId(token)

        // Assert
        extracted shouldBe userId
    }

    @Test
    fun `should return true for a freshly generated token`() {
        // Arrange
        val token = service.generateAccessToken(UUID.randomUUID())

        // Act & Assert
        service.isAccessTokenValid(token).shouldBeTrue()
    }

    @Test
    fun `should return false for an expired token`() {
        // Arrange
        val expiredService =
            JwtServiceImpl(
                validProperties.copy(accessTokenTtl = Duration.ofMillis(-1))
            )
        val token = expiredService.generateAccessToken(UUID.randomUUID())

        // Act & Assert
        service.isAccessTokenValid(token).shouldBeFalse()
    }

    @Test
    fun `should return false for a tampered token`() {
        // Arrange
        val token = service.generateAccessToken(UUID.randomUUID())
        val tampered = token.dropLast(4) + "xxxx"

        // Act & Assert
        service.isAccessTokenValid(tampered).shouldBeFalse()
    }

    @Test
    fun `should return false for a token signed with a different secret`() {
        // Arrange
        val otherService =
            JwtServiceImpl(
                validProperties.copy(secret = "other-secret-key-that-is-at-least-32-characters-long")
            )
        val token = otherService.generateAccessToken(UUID.randomUUID())

        // Act & Assert
        service.isAccessTokenValid(token).shouldBeFalse()
    }

    @Test
    fun `should return the configured refresh token TTL`() {
        // Act & Assert
        service.getRefreshTokenTtl() shouldBe Duration.ofDays(30)
    }
}