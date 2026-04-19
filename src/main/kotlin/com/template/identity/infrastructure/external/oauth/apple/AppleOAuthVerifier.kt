package com.template.identity.infrastructure.external.oauth.apple

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.template.identity.application.result.OAuthUserInfo
import com.template.identity.domain.model.OAuthProvider
import com.template.identity.infrastructure.config.AppProperties
import com.template.identity.infrastructure.external.oauth.ProviderVerifier
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Jwks
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import java.security.PublicKey

@Service
class AppleOAuthVerifier(
    private val appProperties: AppProperties,
) : ProviderVerifier {

    override val provider = OAuthProvider.APPLE

    private val log = LoggerFactory.getLogger(javaClass)
    private val restClient = RestClient.create()

    // Apple uses OIDC — the frontend gets an id_token (JWT) and sends it here.
    // Unlike Google/Microsoft, we verify the JWT signature locally using Apple's public keys
    // fetched from their JWKS endpoint. No per-request HTTP call to Apple after key caching.
    //
    // Important: Apple only sends the user's name on the very first login.
    // Subsequent logins will have no name — store it on first login.
    // Users may also have a private relay email (e.g. xyz@privaterelay.appleid.com).
    override fun verify(idToken: String): OAuthUserInfo {
        val keys = fetchApplePublicKeys()

        val claims = keys.firstNotNullOfOrNull { key ->
            try {
                Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(idToken)
                    .payload
            } catch (_: Exception) {
                null
            }
        } ?: run {
            log.warn("Apple id_token signature verification failed")
            throw RuntimeException("Apple token verification failed")
        }

        val clientId = appProperties.apple.clientId
        if (clientId.isNotBlank() && claims.audience?.firstOrNull() != clientId) {
            log.warn("Apple token audience mismatch — expected $clientId")
            throw RuntimeException("Apple token audience mismatch")
        }

        val email = claims["email"] as? String
            ?: throw RuntimeException("No email claim in Apple token")

        return OAuthUserInfo(
            providerUserId = claims.subject,
            email = email,
            firstName = (claims["given_name"] as? String)?.ifBlank { null }
                ?: email.substringBefore("@"),
            lastName = (claims["family_name"] as? String)?.ifBlank { null } ?: "",
        )
    }

    private fun fetchApplePublicKeys(): List<PublicKey> {
        return try {
            val jwks = restClient.get()
                .uri(appProperties.apple.jwksUrl)
                .retrieve()
                .body<AppleJwks>()
                ?: return emptyList()

            jwks.keys.mapNotNull { jwk ->
                try {
                    Jwks.parser().build().parse(
                        """{"kty":"${jwk.kty}","n":"${jwk.n}","e":"${jwk.e}"}"""
                    ).toKey() as? PublicKey
                } catch (e: Exception) {
                    log.warn("Failed to parse Apple JWK: ${e.javaClass.simpleName}")
                    null
                }
            }
        } catch (e: Exception) {
            log.warn("Failed to fetch Apple public keys: ${e.javaClass.simpleName}")
            throw RuntimeException("Could not fetch Apple public keys", e)
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class AppleJwks(val keys: List<AppleJwk> = emptyList())

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class AppleJwk(
        val kty: String,
        val n: String,
        val e: String,
    )
}
