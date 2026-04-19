package com.template.identity.infrastructure.persistence

import com.template.identity.application.repository.PasswordResetTokenRepository
import com.template.identity.domain.model.PasswordResetToken
import com.template.identity.domain.model.User
import com.template.identity.infrastructure.persistence.jpa.JpaPasswordResetTokenRepository
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class PasswordResetTokenRepositoryImpl(
    private val jpa: JpaPasswordResetTokenRepository
) : PasswordResetTokenRepository {
    override fun findByTokenHash(tokenHash: String): PasswordResetToken? = jpa.findByTokenHash(tokenHash)

    override fun save(token: PasswordResetToken): PasswordResetToken = jpa.save(token)

    override fun deleteByUser(user: User) = jpa.deleteByUser(user)

    override fun deleteExpired() = jpa.deleteExpired(Instant.now())
}