package com.template.identity.infrastructure.persistence.jpa

import com.template.identity.domain.model.PasswordResetToken
import com.template.identity.domain.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.UUID

interface JpaPasswordResetTokenRepository : JpaRepository<PasswordResetToken, UUID> {
    fun findByTokenHash(tokenHash: String): PasswordResetToken?

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM PasswordResetToken p WHERE p.user = :user")
    fun deleteByUser(
        @Param("user") user: User
    )

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM PasswordResetToken p WHERE p.expiresAt < :now")
    fun deleteExpired(
        @Param("now") now: Instant
    )
}