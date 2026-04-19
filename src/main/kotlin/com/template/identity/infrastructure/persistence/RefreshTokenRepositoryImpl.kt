package com.template.identity.infrastructure.persistence

import com.template.identity.application.repository.RefreshTokenRepository
import com.template.identity.domain.model.RefreshToken
import com.template.identity.infrastructure.persistence.jpa.JpaRefreshTokenRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID

@Repository
class RefreshTokenRepositoryImpl(
    private val jpa: JpaRefreshTokenRepository
) : RefreshTokenRepository {
    override fun findByTokenHash(tokenHash: String): RefreshToken? = jpa.findByTokenHash(tokenHash)

    override fun save(token: RefreshToken): RefreshToken = jpa.save(token)

    override fun revoke(tokenId: UUID) = jpa.revokeById(tokenId, Instant.now())

    override fun revokeAllByFamilyId(familyId: UUID) = jpa.revokeAllByFamilyId(familyId, Instant.now())

    override fun deleteExpired() = jpa.deleteExpired(Instant.now())
}