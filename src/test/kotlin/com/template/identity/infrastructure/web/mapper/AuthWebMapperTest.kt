package com.template.identity.infrastructure.web.mapper

import com.template.identity.buildAuthResult
import com.template.identity.buildUser
import com.template.identity.domain.model.OAuthProvider
import com.template.identity.infrastructure.web.dto.request.OAuthLoginRequest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class AuthWebMapperTest {
    @Test
    fun `toResponse should map all fields from AuthenticationResult`() {
        val user = buildUser()
        val result = buildAuthResult(user)

        val response = AuthWebMapper.toResponse(result)

        response.accessToken shouldBe result.accessToken
        response.refreshToken shouldBe result.refreshToken
        response.user.id shouldBe user.id
        response.user.email shouldBe user.email
        response.user.firstName shouldBe user.firstName
        response.user.lastName shouldBe user.lastName
        response.user.isActive shouldBe user.isActive
        response.user.createdAt shouldBe user.createdAt
        response.user.updatedAt shouldBe user.updatedAt
    }

    @Test
    fun `toCommand should parse OAuthLoginRequest with valid provider`() {
        val request = OAuthLoginRequest(provider = "github", idToken = "token-value")

        val command = AuthWebMapper.toCommand(request)

        command.provider shouldBe OAuthProvider.GITHUB
        command.idToken shouldBe "token-value"
    }

    @Test
    fun `toCommand should throw for an unknown OAuth provider`() {
        val request = OAuthLoginRequest(provider = "facebook", idToken = "token-value")

        shouldThrow<IllegalArgumentException> {
            AuthWebMapper.toCommand(request)
        }
    }
}