package com.template.identity.infrastructure.external.oauth.github

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.template.identity.application.result.OAuthUserInfo
import com.template.identity.domain.model.OAuthProvider
import com.template.identity.infrastructure.config.AppProperties
import com.template.identity.infrastructure.external.oauth.ProviderVerifier
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

@Service
class GitHubOAuthVerifier(
    private val appProperties: AppProperties,
) : ProviderVerifier {

    override val provider = OAuthProvider.GITHUB

    private val log = LoggerFactory.getLogger(javaClass)
    private val restClient = RestClient.create()

    // GitHub uses OAuth 2.0 access tokens, not OIDC id_tokens.
    // The frontend exchanges the authorization code for an access token and sends that here.
    override fun verify(idToken: String): OAuthUserInfo {
        val user = try {
            restClient.get()
                .uri("${appProperties.github.apiUrl}/user")
                .header("Authorization", "Bearer $idToken")
                .header("Accept", "application/vnd.github+json")
                .retrieve()
                .body<GitHubUser>()
        } catch (e: Exception) {
            log.warn("GitHub user request failed: ${e.javaClass.simpleName}")
            throw RuntimeException("GitHub token verification failed", e)
        } ?: throw RuntimeException("Empty response from GitHub API")

        // GitHub users can hide their email — fetch the primary verified one if needed
        val email = user.email ?: fetchPrimaryEmail(idToken)
        ?: throw RuntimeException("No verified email found for GitHub user")

        val firstName = user.name?.substringBefore(" ") ?: email.substringBefore("@")
        val lastName = user.name?.substringAfter(" ", "")?.ifBlank { null } ?: ""

        return OAuthUserInfo(
            providerUserId = user.id.toString(),
            email = email,
            firstName = firstName,
            lastName = lastName,
        )
    }

    private fun fetchPrimaryEmail(accessToken: String): String? {
        return try {
            restClient.get()
                .uri("${appProperties.github.apiUrl}/user/emails")
                .header("Authorization", "Bearer $accessToken")
                .header("Accept", "application/vnd.github+json")
                .retrieve()
                .body<List<GitHubEmail>>()
                ?.firstOrNull { it.primary && it.verified }
                ?.email
        } catch (e: Exception) {
            log.warn("GitHub email fetch failed: ${e.javaClass.simpleName}")
            null
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class GitHubUser(
        val id: Long,
        val email: String?,
        val name: String?,
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class GitHubEmail(
        val email: String,
        val primary: Boolean,
        val verified: Boolean,
    )
}
