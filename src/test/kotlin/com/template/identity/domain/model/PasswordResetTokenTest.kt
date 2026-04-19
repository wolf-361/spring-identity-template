package com.template.identity.domain.model

import com.template.identity.buildPasswordResetToken
import com.template.identity.buildUser
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import org.junit.jupiter.api.Test
import java.time.Instant

class PasswordResetTokenTest {
    private val user = buildUser()

    @Test
    fun `isExpired should return false when token has not yet expired`() {
        val token = buildPasswordResetToken(user, expiresAt = Instant.now().plusSeconds(3_600))
        token.isExpired().shouldBeFalse()
    }

    @Test
    fun `isExpired should return true when token TTL has passed`() {
        val token = buildPasswordResetToken(user, expiresAt = Instant.now().minusSeconds(1))
        token.isExpired().shouldBeTrue()
    }
}