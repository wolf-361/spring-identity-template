package com.template.identity.domain.model

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "refresh_tokens")
class RefreshToken(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    var user: User,

    /** SHA-256 hash of the raw opaque token. The raw token is never stored. */
    @Column(name = "token_hash", nullable = false, unique = true, updatable = false)
    var tokenHash: String,

    /**
     * Groups tokens in a rotation chain.
     * Reuse of a revoked token triggers revocation of every token sharing this ID,
     * forcing full re-authentication across all sessions in the chain.
     */
    @Column(name = "family_id", nullable = false, updatable = false)
    var familyId: UUID,

    @Column(name = "expires_at", nullable = false, updatable = false)
    var expiresAt: Instant,

    /** Non-null when the token has been rotated out or explicitly revoked on logout. */
    @Column(name = "revoked_at")
    var revokedAt: Instant? = null,
) : AuditableEntity() {

    /** Returns true if the token's TTL has passed regardless of revocation status. */
    fun isExpired(): Boolean = Instant.now().isAfter(expiresAt)

    /** Returns true if the token has been rotated out or explicitly revoked. */
    fun isRevoked(): Boolean = revokedAt != null

    /** Returns true only if the token is both unexpired and not revoked. */
    fun isValid(): Boolean = !isExpired() && !isRevoked()
}
