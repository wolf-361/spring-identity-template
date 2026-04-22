package com.template.identity

import com.template.identity.infrastructure.config.FlywayEnvironmentListener
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
@ConfigurationPropertiesScan
class IdentityApplication

fun main(args: Array<String>) {
    val application = SpringApplication(IdentityApplication::class.java)
    application.addListeners(FlywayEnvironmentListener())
    application.run(*args)
}