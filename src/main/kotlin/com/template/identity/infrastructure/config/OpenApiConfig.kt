package com.template.identity.infrastructure.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {
    @Bean
    fun identityOpenApi(): OpenAPI =
        OpenAPI()
            .info(
                Info()
                    .title("Identity Service API")
                    .version("1.0")
                    .description(
                        "Authentication, authorization, and user management.\n\n" +
                            "Endpoints under `/auth/**` are public. All others require a Bearer JWT except for the public profile"
                    )
            ).addSecurityItem(SecurityRequirement().addList("bearerAuth"))
            .components(
                Components().addSecuritySchemes(
                    "bearerAuth",
                    SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                )
            )
}