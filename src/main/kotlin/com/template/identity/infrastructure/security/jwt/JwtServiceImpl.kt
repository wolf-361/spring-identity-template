package com.template.identity.infrastructure.security.jwt

import com.template.identity.application.service.JwtService
import com.template.identity.infrastructure.config.JwtProperties
import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.Date
import java.util.UUID
import javax.crypto.SecretKey

@Service
class JwtServiceImpl(
    private val jwtProperties: JwtProperties
) : JwtService {
    private val signingKey: SecretKey by lazy {
        Keys.hmacShaKeyFor(jwtProperties.secret.toByteArray())
    }

    override fun generateAccessToken(userId: UUID): String =
        Jwts
            .builder()
            .subject(userId.toString())
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + jwtProperties.accessTokenTtl.toMillis()))
            .signWith(signingKey)
            .compact()

    override fun extractUserId(token: String): UUID = UUID.fromString(extractAllClaims(token).subject)

    override fun isAccessTokenValid(token: String): Boolean =
        try {
            !extractAllClaims(token).expiration.before(Date())
        } catch (e: JwtException) {
            false
        } catch (e: IllegalArgumentException) {
            false
        }

    override fun getRefreshTokenTtl(): Duration = jwtProperties.refreshTokenTtl

    private fun extractAllClaims(token: String): Claims =
        Jwts
            .parser()
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(token)
            .payload
}