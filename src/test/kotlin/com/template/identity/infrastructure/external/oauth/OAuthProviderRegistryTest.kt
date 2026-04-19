package com.template.identity.infrastructure.external.oauth

import com.template.identity.application.exception.ApplicationException
import com.template.identity.buildOAuthUserInfo
import com.template.identity.domain.model.OAuthProvider
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test

class OAuthProviderRegistryTest {
    private val googleVerifier: ProviderVerifier =
        mockk {
            every { provider } returns OAuthProvider.GOOGLE
        }

    @Test
    fun `should dispatch to the correct verifier for the given provider`() {
        // Arrange
        val registry = OAuthProviderRegistry(listOf(googleVerifier))
        val userInfo = buildOAuthUserInfo()
        every { googleVerifier.verify("id-token") } returns userInfo

        // Act
        val result = registry.verify(OAuthProvider.GOOGLE, "id-token")

        // Assert
        result shouldBe userInfo
    }

    @Test
    fun `should throw OAuthProviderNotSupported when no verifier is registered for the provider`() {
        // Arrange — registry with no verifiers
        val registry = OAuthProviderRegistry(emptyList())

        // Act & Assert
        shouldThrow<ApplicationException.OAuthProviderNotSupported> {
            registry.verify(OAuthProvider.GOOGLE, "id-token")
        }
    }
}