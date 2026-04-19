package com.template.identity.infrastructure.external.oauth.microsoft

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
class MicrosoftOAuthVerifier(
    private val appProperties: AppProperties,
) : ProviderVerifier {

    override val provider = OAuthProvider.MICROSOFT

    private val log = LoggerFactory.getLogger(javaClass)
    private val restClient = RestClient.create()

    // Microsoft uses OIDC — the frontend gets an id_token and sends it here.
    // We call the userinfo endpoint with the token as Bearer to get user claims.
    // tenantId defaults to "common" (personal + work accounts). Set to your Azure tenant ID to restrict to one org.
    override fun verify(idToken: String): OAuthUserInfo {
        val tenantId = appProperties.microsoft.tenantId
        val claims = try {
            restClient.get()
                .uri("${appProperties.microsoft.userInfoUrl}/$tenantId/v2.0/userinfo")
                .header("Authorization", "Bearer $idToken")
                .retrieve()
                .body<MicrosoftUserClaims>()
        } catch (e: Exception) {
            log.warn("Microsoft token verification request failed: ${e.javaClass.simpleName}")
            throw RuntimeException("Microsoft token verification failed", e)
        } ?: throw RuntimeException("Empty response from Microsoft userinfo")

        val clientId = appProperties.microsoft.clientId
        if (clientId.isNotBlank() && claims.aud != clientId) {
            log.warn("Microsoft token audience mismatch — expected $clientId")
            throw RuntimeException("Microsoft token audience mismatch")
        }

        return OAuthUserInfo(
            providerUserId = claims.sub,
            email = claims.email,
            firstName = claims.givenName.ifBlank { claims.email.substringBefore("@") },
            lastName = claims.familyName,
        )
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class MicrosoftUserClaims(
        val sub: String,
        val email: String,
        @field:JsonProperty("given_name")
        val givenName: String = "",
        @field:JsonProperty("family_name")
        val familyName: String = "",
        val aud: String = "",
    )
}
