package com.template.identity.domain.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "password_reset_tokens")
class PasswordResetToken(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    var user: User,
    /** SHA-256 hash of the raw token sent in the reset link. Never stored raw. */
    @Column(name = "token_hash", nullable = false, unique = true, updatable = false)
    var tokenHash: String,
    @Column(name = "expires_at", nullable = false, updatable = false)
    var expiresAt: Instant
) : AuditableEntity() {
    /** Returns true if the 1-hour TTL has passed. Expired tokens are deleted before throwing. */
    fun isExpired(): Boolean = Instant.now().isAfter(expiresAt)
}