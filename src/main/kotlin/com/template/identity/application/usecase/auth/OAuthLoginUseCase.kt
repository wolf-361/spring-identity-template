package com.template.identity.application.usecase.auth

import com.template.identity.application.command.OAuthLoginCommand
import com.template.identity.application.exception.ApplicationException
import com.template.identity.application.repository.OAuthAccountRepository
import com.template.identity.application.repository.UserRepository
import com.template.identity.application.result.AuthenticationResult
import com.template.identity.application.service.OAuthVerifier
import com.template.identity.application.service.TokenPairIssuer
import com.template.identity.domain.model.OAuthAccount
import com.template.identity.domain.model.User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Handles OAuth login for first-time sign-up and returning users in a single flow.
 *
 * Resolution order:
 * 1. Existing OAuth account → update email, return user.
 * 2. No OAuth account but email matches existing user → link the new provider.
 * 3. No match at all → create a new user and link the provider.
 *
 * @throws ApplicationException.OAuthProviderNotSupported if the provider has no registered verifier.
 * @throws ApplicationException.OAuthAccountAlreadyLinked if the verified OAuth account belongs to a different user.
 * @throws ApplicationException.InvalidCredentials if the resolved account is inactive.
 */
@Service
class OAuthLoginUseCase(
    private val oauthVerifier: OAuthVerifier,
    private val userRepository: UserRepository,
    private val oauthAccountRepository: OAuthAccountRepository,
    private val tokenPairIssuer: TokenPairIssuer
) {
    @Transactional
    fun execute(command: OAuthLoginCommand): AuthenticationResult {
        val userInfo = oauthVerifier.verify(command.provider, command.idToken)

        val existingAccount =
            oauthAccountRepository.findByProviderAndProviderUserId(
                command.provider,
                userInfo.providerUserId
            )

        val user =
            if (existingAccount != null) {
                existingAccount.providerEmail = userInfo.email
                oauthAccountRepository.save(existingAccount)
                existingAccount.user
            } else {
                val existingUser = userRepository.findByEmail(userInfo.email)

                if (existingUser != null) {
                    val alreadyLinked =
                        oauthAccountRepository
                            .findByUserAndProvider(existingUser, command.provider) != null
                    if (alreadyLinked) throw ApplicationException.OAuthAccountAlreadyLinked()

                    oauthAccountRepository.save(
                        OAuthAccount(
                            user = existingUser,
                            provider = command.provider,
                            providerUserId = userInfo.providerUserId,
                            providerEmail = userInfo.email
                        )
                    )
                    existingUser
                } else {
                    val newUser =
                        userRepository.save(
                            User(
                                email = userInfo.email,
                                firstName = userInfo.firstName,
                                lastName = userInfo.lastName
                            )
                        )
                    oauthAccountRepository.save(
                        OAuthAccount(
                            user = newUser,
                            provider = command.provider,
                            providerUserId = userInfo.providerUserId,
                            providerEmail = userInfo.email
                        )
                    )
                    newUser
                }
            }

        if (!user.isActive) throw ApplicationException.InvalidCredentials()

        return tokenPairIssuer.issue(user)
    }
}