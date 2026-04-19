package com.template.identity.infrastructure.external.oauth.google

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.template.identity.application.result.OAuthUserInfo
import com.template.identity.domain.model.OAuthProvider
import com.template.identity.infrastructure.config.AppProperties
import com.template.identity.infrastructure.external.oauth.ProviderVerifier
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

@Service
class GoogleOAuthVerifier(
    private val appProperties: AppProperties,
) : ProviderVerifier {

    override val provider = OAuthProvider.GOOGLE

    private val log = LoggerFactory.getLogger(javaClass)
    private val restClient = RestClient.create()

    override fun verify(idToken: String): OAuthUserInfo {
        val claims = try {
            restClient.get()
                .uri("https://oauth2.googleapis.com/tokeninfo?id_token={token}", idToken)
                .retrieve()
                .body<GoogleTokenClaims>()
        } catch (e: Exception) {
            log.warn("Google token verification request failed: ${e.javaClass.simpleName}")
            throw RuntimeException("Google token verification failed", e)
        } ?: throw RuntimeException("Empty response from Google tokeninfo")

        val clientId = appProperties.google.clientId
        if (clientId.isNotBlank() && claims.aud != clientId) {
            log.warn("Google token audience mismatch — expected $clientId")
            throw RuntimeException("Google token audience mismatch")
        }

        return OAuthUserInfo(
            providerUserId = claims.sub,
            email = claims.email,
            firstName = claims.givenName.ifBlank { claims.email.substringBefore("@") },
            lastName = claims.familyName,
        )
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class GoogleTokenClaims(
        val sub: String,
        val email: String,
        @JsonProperty("given_name") val givenName: String = "",
        @JsonProperty("family_name") val familyName: String = "",
        val aud: String,
    )
}
