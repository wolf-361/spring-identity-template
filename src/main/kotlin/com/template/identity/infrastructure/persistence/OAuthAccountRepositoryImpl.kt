package com.template.identity.infrastructure.persistence

import com.template.identity.application.repository.OAuthAccountRepository
import com.template.identity.domain.model.OAuthAccount
import com.template.identity.domain.model.OAuthProvider
import com.template.identity.domain.model.User
import com.template.identity.infrastructure.persistence.jpa.JpaOAuthAccountRepository
import org.springframework.stereotype.Repository

@Repository
class OAuthAccountRepositoryImpl(
    private val jpa: JpaOAuthAccountRepository,
) : OAuthAccountRepository {

    override fun findByProviderAndProviderUserId(
        provider: OAuthProvider,
        providerUserId: String,
    ): OAuthAccount? = jpa.findByProviderAndProviderUserId(provider, providerUserId)

    override fun findByUserAndProvider(user: User, provider: OAuthProvider): OAuthAccount? =
        jpa.findByUserAndProvider(user, provider)

    override fun countByUser(user: User): Int = jpa.countByUser(user).toInt()

    override fun save(account: OAuthAccount): OAuthAccount = jpa.save(account)

    override fun delete(account: OAuthAccount) = jpa.delete(account)
}
