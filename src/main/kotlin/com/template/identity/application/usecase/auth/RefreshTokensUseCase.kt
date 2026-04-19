package com.template.identity.application.usecase.auth

import com.template.identity.application.command.RefreshTokensCommand
import com.template.identity.application.result.AuthenticationResult
import com.template.identity.application.exception.ApplicationException
import com.template.identity.application.repository.RefreshTokenRepository
import com.template.identity.application.service.TokenPairIssuer
import com.template.identity.application.sha256
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Rotates a refresh token and issues a new token pair.
 *
 * On each call the presented token is revoked and a new one is issued within the same family,
 * preserving the rotation chain for reuse detection (ADR-0003).
 *
 * @throws ApplicationException.RefreshTokenInvalid if the token hash is not found.
 * @throws ApplicationException.RefreshTokenRevoked if the token was already revoked — triggers
 *   family-wide revocation before throwing, forcing full re-authentication.
 * @throws ApplicationException.RefreshTokenExpired if the token TTL has passed.
 */
@Service
class RefreshTokensUseCase(
    private val refreshTokenRepository: RefreshTokenRepository,
    private val tokenPairIssuer: TokenPairIssuer,
) {
    @Transactional
    fun execute(command: RefreshTokensCommand): AuthenticationResult {
        val tokenHash = sha256(command.rawRefreshToken)
        val token = refreshTokenRepository.findByTokenHash(tokenHash)
            ?: throw ApplicationException.RefreshTokenInvalid()

        if (token.isRevoked()) {
            refreshTokenRepository.revokeAllByFamilyId(token.familyId)
            throw ApplicationException.RefreshTokenRevoked()
        }

        if (token.isExpired()) throw ApplicationException.RefreshTokenExpired()

        refreshTokenRepository.revoke(token.id!!)

        return tokenPairIssuer.issue(token.user, familyId = token.familyId)
    }
}
