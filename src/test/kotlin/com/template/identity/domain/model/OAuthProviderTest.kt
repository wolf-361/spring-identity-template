package com.template.identity.domain.model

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class OAuthProviderTest {
    @Test
    fun `should parse provider case-insensitively`() {
        OAuthProvider.fromString("google") shouldBe OAuthProvider.GOOGLE
        OAuthProvider.fromString("GITHUB") shouldBe OAuthProvider.GITHUB
        OAuthProvider.fromString("Apple") shouldBe OAuthProvider.APPLE
        OAuthProvider.fromString("MICROSOFT") shouldBe OAuthProvider.MICROSOFT
    }

    @Test
    fun `should throw for an unknown provider`() {
        shouldThrow<IllegalArgumentException> {
            OAuthProvider.fromString("facebook")
        }
    }
}