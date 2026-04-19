package com.template.identity.infrastructure.persistence.jpa

import com.template.identity.domain.model.RefreshToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.UUID

interface JpaRefreshTokenRepository : JpaRepository<RefreshToken, UUID> {
    fun findByTokenHash(tokenHash: String): RefreshToken?

    @Modifying(clearAutomatically = true)
    @Query("UPDATE RefreshToken r SET r.revokedAt = :now WHERE r.id = :id")
    fun revokeById(
        @Param("id") id: UUID,
        @Param("now") now: Instant
    )

    @Modifying(clearAutomatically = true)
    @Query("UPDATE RefreshToken r SET r.revokedAt = :now WHERE r.familyId = :familyId")
    fun revokeAllByFamilyId(
        @Param("familyId") familyId: UUID,
        @Param("now") now: Instant
    )

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM RefreshToken r WHERE r.expiresAt < :now")
    fun deleteExpired(
        @Param("now") now: Instant
    )
}