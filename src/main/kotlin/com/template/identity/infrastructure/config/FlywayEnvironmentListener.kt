package com.template.identity.infrastructure.config

import org.flywaydb.core.Flyway
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent
import org.springframework.context.ApplicationListener
import kotlin.system.exitProcess

class FlywayEnvironmentListener : ApplicationListener<ApplicationEnvironmentPreparedEvent> {
    private val log = LoggerFactory.getLogger(this::class.java)

    override fun onApplicationEvent(event: ApplicationEnvironmentPreparedEvent) {
        val env = event.environment
        val url = env.getProperty("spring.datasource.url")
        val user = env.getProperty("spring.datasource.username")
        val pass = env.getProperty("spring.datasource.password")

        log.info("Bootstrapping Flyway using application.yml credentials...")

        try {
            val flyway =
                Flyway
                    .configure()
                    .dataSource(url, user, pass)
                    .locations("classpath:db/migration")
                    .baselineOnMigrate(true)
                    .load()

            val result = flyway.migrate()
            log.info("Flyway execution completed. Migrations applied: ${result.migrationsExecuted}")
        } catch (e: Exception) {
            log.error("FLYWAY MIGRATION FAILED: ${e.message}", e)
            exitProcess(1) // Crash the Docker container immediately if the DB is unreachable
        }
    }
}