package com.template.identity.domain.model

import com.template.identity.buildRefreshToken
import com.template.identity.buildUser
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import org.junit.jupiter.api.Test
import java.time.Instant

class RefreshTokenTest {
    private val user = buildUser()

    @Test
    fun `isExpired should return false when token has not yet expired`() {
        val token = buildRefreshToken(user, expiresAt = Instant.now().plusSeconds(3_600))
        token.isExpired().shouldBeFalse()
    }

    @Test
    fun `isExpired should return true when token TTL has passed`() {
        val token = buildRefreshToken(user, expiresAt = Instant.now().minusSeconds(1))
        token.isExpired().shouldBeTrue()
    }

    @Test
    fun `isRevoked should return false when revokedAt is null`() {
        val token = buildRefreshToken(user, revokedAt = null)
        token.isRevoked().shouldBeFalse()
    }

    @Test
    fun `isRevoked should return true when revokedAt is set`() {
        val token = buildRefreshToken(user, revokedAt = Instant.now())
        token.isRevoked().shouldBeTrue()
    }

    @Test
    fun `isValid should return true when token is unexpired and not revoked`() {
        val token = buildRefreshToken(user, expiresAt = Instant.now().plusSeconds(3_600), revokedAt = null)
        token.isValid().shouldBeTrue()
    }

    @Test
    fun `isValid should return false when token is expired`() {
        val token = buildRefreshToken(user, expiresAt = Instant.now().minusSeconds(1), revokedAt = null)
        token.isValid().shouldBeFalse()
    }

    @Test
    fun `isValid should return false when token is revoked`() {
        val token = buildRefreshToken(user, expiresAt = Instant.now().plusSeconds(3_600), revokedAt = Instant.now())
        token.isValid().shouldBeFalse()
    }
}