package com.template.identity.infrastructure.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app")
data class AppProperties(
    val frontendUrl: String,
    val cors: CorsProperties,
    val google: GoogleProperties = GoogleProperties(),
    val email: EmailProperties = EmailProperties(),
) {
    data class CorsProperties(val allowedOrigins: List<String> = emptyList())
    data class GoogleProperties(val clientId: String = "")
    data class EmailProperties(val fromAddress: String = "noreply@example.com")
}
