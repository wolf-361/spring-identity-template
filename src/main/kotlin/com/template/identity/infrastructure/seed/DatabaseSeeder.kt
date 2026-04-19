package com.template.identity.infrastructure.seed

import com.template.identity.application.repository.UserRepository
import com.template.identity.application.service.PasswordEncoder
import com.template.identity.domain.model.User
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("dev")
class DatabaseSeeder(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) : CommandLineRunner {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun run(vararg args: String) {
        if (userRepository.existsByEmail(DEV_EMAIL)) return

        userRepository.save(
            User(
                email = DEV_EMAIL,
                password = passwordEncoder.encode("password"),
                firstName = "Dev",
                lastName = "User"
            )
        )
        log.info("Seeded dev user: {} / password", DEV_EMAIL)
    }

    companion object {
        private const val DEV_EMAIL = "dev@example.com"
    }
}