package com.template.identity.application.service

import com.template.identity.application.generateSecureToken
import com.template.identity.application.repository.RefreshTokenRepository
import com.template.identity.application.result.AuthenticationResult
import com.template.identity.application.sha256
import com.template.identity.domain.model.RefreshToken
import com.template.identity.domain.model.User
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

@Service
class TokenPairIssuer(
    private val jwtService: JwtService,
    private val refreshTokenRepository: RefreshTokenRepository
) {
    fun issue(
        user: User,
        familyId: UUID = UUID.randomUUID()
    ): AuthenticationResult {
        val rawToken = generateSecureToken()
        val refreshToken =
            RefreshToken(
                user = user,
                tokenHash = sha256(rawToken),
                familyId = familyId,
                expiresAt = Instant.now().plus(jwtService.getRefreshTokenTtl())
            )
        refreshTokenRepository.save(refreshToken)

        return AuthenticationResult(
            accessToken = jwtService.generateAccessToken(user.id!!),
            refreshToken = rawToken,
            user = user
        )
    }
}