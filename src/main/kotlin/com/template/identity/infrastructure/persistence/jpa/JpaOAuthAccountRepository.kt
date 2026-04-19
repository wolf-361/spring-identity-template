package com.template.identity.infrastructure.persistence.jpa

import com.template.identity.domain.model.OAuthAccount
import com.template.identity.domain.model.OAuthProvider
import com.template.identity.domain.model.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface JpaOAuthAccountRepository : JpaRepository<OAuthAccount, UUID> {
    fun findByProviderAndProviderUserId(provider: OAuthProvider, providerUserId: String): OAuthAccount?
    fun findByUserAndProvider(user: User, provider: OAuthProvider): OAuthAccount?
    fun countByUser(user: User): Long
}
