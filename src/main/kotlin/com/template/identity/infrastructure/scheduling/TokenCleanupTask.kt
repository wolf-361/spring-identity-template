package com.template.identity.infrastructure.scheduling

import com.template.identity.application.repository.PasswordResetTokenRepository
import com.template.identity.application.repository.RefreshTokenRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class TokenCleanupTask(
    private val refreshTokenRepository: RefreshTokenRepository,
    private val passwordResetTokenRepository: PasswordResetTokenRepository
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    fun cleanExpiredTokens() {
        refreshTokenRepository.deleteExpired()
        passwordResetTokenRepository.deleteExpired()
        log.info("Expired token cleanup completed")
    }
}