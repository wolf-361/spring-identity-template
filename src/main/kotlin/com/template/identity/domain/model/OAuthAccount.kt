package com.template.identity.domain.model

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(
    name = "oauth_accounts",
    uniqueConstraints = [UniqueConstraint(columnNames = ["provider", "provider_user_id"])]
)
class OAuthAccount(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    var user: User,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    var provider: OAuthProvider,

    @Column(name = "provider_user_id", nullable = false, updatable = false)
    var providerUserId: String,

    @Column(name = "provider_email", nullable = false)
    var providerEmail: String,
) : AuditableEntity()
