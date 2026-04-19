package com.template.identity.infrastructure.external.oauth.microsoft

import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import com.template.identity.infrastructure.config.AppProperties
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

@WireMockTest
class MicrosoftOAuthVerifierTest {
    private fun buildVerifier(
        runtimeInfo: WireMockRuntimeInfo,
        clientId: String = ""
    ) = MicrosoftOAuthVerifier(
        AppProperties(
            frontendUrl = "http://localhost",
            cors = AppProperties.CorsProperties(),
            microsoft =
                AppProperties.MicrosoftProperties(
                    clientId = clientId,
                    userInfoUrl = runtimeInfo.httpBaseUrl
                )
        )
    )

    @Test
    fun `should return user info from userinfo endpoint`(runtimeInfo: WireMockRuntimeInfo) {
        runtimeInfo.wireMock.register(
            get("/common/v2.0/userinfo").willReturn(
                okJson(
                    """{"sub":"ms-user-id","email":"user@outlook.com","given_name":"Jane","family_name":"Doe","aud":"client"}"""
                )
            )
        )

        val result = buildVerifier(runtimeInfo).verify("id-token")

        result.providerUserId shouldBe "ms-user-id"
        result.email shouldBe "user@outlook.com"
        result.firstName shouldBe "Jane"
        result.lastName shouldBe "Doe"
    }

    @Test
    fun `should use email prefix as first name when given_name is blank`(runtimeInfo: WireMockRuntimeInfo) {
        runtimeInfo.wireMock.register(
            get("/common/v2.0/userinfo").willReturn(
                okJson("""{"sub":"ms-user-id","email":"jane@outlook.com","given_name":"","family_name":"","aud":""}""")
            )
        )

        val result = buildVerifier(runtimeInfo).verify("id-token")

        result.firstName shouldBe "jane"
    }

    @Test
    fun `should throw when audience does not match configured client ID`(runtimeInfo: WireMockRuntimeInfo) {
        runtimeInfo.wireMock.register(
            get("/common/v2.0/userinfo").willReturn(
                okJson(
                    """{"sub":"ms-user-id","email":"user@outlook.com","given_name":"Jane","family_name":"Doe","aud":"wrong-client"}"""
                )
            )
        )

        shouldThrow<RuntimeException> {
            buildVerifier(runtimeInfo, clientId = "expected-client").verify("id-token")
        }
    }
}