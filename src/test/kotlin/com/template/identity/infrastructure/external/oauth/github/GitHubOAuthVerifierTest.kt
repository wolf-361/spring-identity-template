package com.template.identity.infrastructure.external.oauth.github

import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import com.template.identity.infrastructure.config.AppProperties
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

@WireMockTest
class GitHubOAuthVerifierTest {
    private fun buildVerifier(runtimeInfo: WireMockRuntimeInfo) =
        GitHubOAuthVerifier(
            AppProperties(
                frontendUrl = "http://localhost",
                cors = AppProperties.CorsProperties(),
                github = AppProperties.GithubProperties(apiUrl = runtimeInfo.httpBaseUrl)
            )
        )

    @Test
    fun `should return user info when email is present in user response`(runtimeInfo: WireMockRuntimeInfo) {
        runtimeInfo.wireMock.register(
            get("/user").willReturn(okJson("""{"id":1234,"email":"user@github.com","name":"Jane Doe"}"""))
        )

        val result = buildVerifier(runtimeInfo).verify("access-token")

        result.providerUserId shouldBe "1234"
        result.email shouldBe "user@github.com"
        result.firstName shouldBe "Jane"
        result.lastName shouldBe "Doe"
    }

    @Test
    fun `should fall back to emails endpoint when user email is null`(runtimeInfo: WireMockRuntimeInfo) {
        runtimeInfo.wireMock.register(
            get("/user").willReturn(okJson("""{"id":1234,"email":null,"name":"Jane Doe"}"""))
        )
        runtimeInfo.wireMock.register(
            get("/user/emails").willReturn(
                okJson("""[{"email":"private@users.noreply.github.com","primary":true,"verified":true}]""")
            )
        )

        val result = buildVerifier(runtimeInfo).verify("access-token")

        result.email shouldBe "private@users.noreply.github.com"
    }

    @Test
    fun `should use email prefix as first name when user has no name`(runtimeInfo: WireMockRuntimeInfo) {
        runtimeInfo.wireMock.register(
            get("/user").willReturn(okJson("""{"id":1234,"email":"jane@github.com","name":null}"""))
        )

        val result = buildVerifier(runtimeInfo).verify("access-token")

        result.firstName shouldBe "jane"
        result.lastName shouldBe ""
    }

    @Test
    fun `should throw when email is null and no verified email found`(runtimeInfo: WireMockRuntimeInfo) {
        runtimeInfo.wireMock.register(
            get("/user").willReturn(okJson("""{"id":1234,"email":null,"name":null}"""))
        )
        runtimeInfo.wireMock.register(
            get("/user/emails").willReturn(okJson("[]"))
        )

        shouldThrow<RuntimeException> {
            buildVerifier(runtimeInfo).verify("access-token")
        }
    }
}