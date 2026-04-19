package com.template.identity.application.usecase.auth

import com.template.identity.application.command.LogoutCommand
import com.template.identity.application.repository.RefreshTokenRepository
import com.template.identity.application.sha256
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Revokes the presented refresh token, effectively ending the session.
 *
 * Silent no-op if the token is not found — logout should always succeed from the client's perspective.
 * The access token expires naturally within its 15-minute TTL.
 */
@Service
class LogoutUseCase(
    private val refreshTokenRepository: RefreshTokenRepository,
) {
    @Transactional
    fun execute(command: LogoutCommand) {
        val tokenHash = sha256(command.rawRefreshToken)
        val token = refreshTokenRepository.findByTokenHash(tokenHash) ?: return
        refreshTokenRepository.revoke(token.id!!)
    }
}
