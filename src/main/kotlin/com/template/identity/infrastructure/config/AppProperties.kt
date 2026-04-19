package com.template.identity.infrastructure.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app")
data class AppProperties(
    val frontendUrl: String,
    val cors: CorsProperties,
    val google: GoogleProperties = GoogleProperties(),
    val github: GithubProperties = GithubProperties(),
    val apple: AppleProperties = AppleProperties(),
    val microsoft: MicrosoftProperties = MicrosoftProperties(),
    val email: EmailProperties = EmailProperties()
) {
    data class CorsProperties(
        val allowedOrigins: List<String> = emptyList()
    )

    data class GoogleProperties(
        val clientId: String = "",
        val tokenInfoUrl: String = "https://oauth2.googleapis.com/tokeninfo"
    )

    data class GithubProperties(
        val clientId: String = "",
        val apiUrl: String = "https://api.github.com"
    )

    data class AppleProperties(
        val clientId: String = "",
        val jwksUrl: String = "https://appleid.apple.com/auth/keys"
    )

    data class MicrosoftProperties(
        val clientId: String = "",
        val tenantId: String = "common",
        val userInfoUrl: String = "https://login.microsoftonline.com"
    )

    data class EmailProperties(
        val fromAddress: String = "noreply@example.com"
    )
}