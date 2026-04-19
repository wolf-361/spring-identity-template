package com.template.identity.infrastructure.external.oauth.apple

import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import com.github.tomakehurst.wiremock.client.WireMock.serverError
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import com.template.identity.infrastructure.config.AppProperties
import io.jsonwebtoken.Jwts
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPublicKey
import java.util.Base64

@WireMockTest
class AppleOAuthVerifierTest {
    private val keyPair = KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }.generateKeyPair()

    private fun buildVerifier(runtimeInfo: WireMockRuntimeInfo) =
        AppleOAuthVerifier(
            AppProperties(
                frontendUrl = "http://localhost",
                cors = AppProperties.CorsProperties(),
                apple = AppProperties.AppleProperties(jwksUrl = "${runtimeInfo.httpBaseUrl}/auth/keys")
            )
        )

    private fun signedToken(
        subject: String = "apple-user-id",
        email: String = "user@privaterelay.appleid.com",
        givenName: String = "Jane",
        familyName: String = "Doe"
    ): String =
        Jwts
            .builder()
            .subject(subject)
            .claim("email", email)
            .claim("given_name", givenName)
            .claim("family_name", familyName)
            .signWith(keyPair.private)
            .compact()

    private fun stubJwks(runtimeInfo: WireMockRuntimeInfo) {
        val pub = keyPair.public as RSAPublicKey

        fun encode(bytes: ByteArray): String {
            val stripped = if (bytes[0] == 0.toByte()) bytes.copyOfRange(1, bytes.size) else bytes
            return Base64.getUrlEncoder().withoutPadding().encodeToString(stripped)
        }
        val n = encode(pub.modulus.toByteArray())
        val e = encode(pub.publicExponent.toByteArray())
        runtimeInfo.wireMock.register(
            get("/auth/keys").willReturn(
                okJson("""{"keys":[{"kty":"RSA","n":"$n","e":"$e"}]}""")
            )
        )
    }

    @Test
    fun `should verify token and return user info`(runtimeInfo: WireMockRuntimeInfo) {
        stubJwks(runtimeInfo)

        val result = buildVerifier(runtimeInfo).verify(signedToken())

        result.providerUserId shouldBe "apple-user-id"
        result.email shouldBe "user@privaterelay.appleid.com"
        result.firstName shouldBe "Jane"
        result.lastName shouldBe "Doe"
    }

    @Test
    fun `should use email prefix as first name when given_name is absent`(runtimeInfo: WireMockRuntimeInfo) {
        stubJwks(runtimeInfo)
        val token = signedToken(email = "abc@privaterelay.appleid.com", givenName = "")

        val result = buildVerifier(runtimeInfo).verify(token)

        result.firstName shouldBe "abc"
    }

    @Test
    fun `should throw when JWKS endpoint is unavailable`(runtimeInfo: WireMockRuntimeInfo) {
        runtimeInfo.wireMock.register(get("/auth/keys").willReturn(serverError()))

        shouldThrow<RuntimeException> {
            buildVerifier(runtimeInfo).verify(signedToken())
        }
    }

    @Test
    fun `should throw when token is signed with an unknown key`(runtimeInfo: WireMockRuntimeInfo) {
        // Serve a JWKS from a different key pair so verification fails
        val otherKeyPair = KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }.generateKeyPair()
        val otherPub = otherKeyPair.public as RSAPublicKey

        fun encode(bytes: ByteArray): String {
            val stripped = if (bytes[0] == 0.toByte()) bytes.copyOfRange(1, bytes.size) else bytes
            return Base64.getUrlEncoder().withoutPadding().encodeToString(stripped)
        }
        runtimeInfo.wireMock.register(
            get("/auth/keys").willReturn(
                okJson(
                    """{"keys":[{"kty":"RSA","n":"${encode(
                        otherPub.modulus.toByteArray()
                    )}","e":"${encode(otherPub.publicExponent.toByteArray())}"}]}"""
                )
            )
        )

        shouldThrow<RuntimeException> {
            buildVerifier(runtimeInfo).verify(signedToken())
        }
    }
}