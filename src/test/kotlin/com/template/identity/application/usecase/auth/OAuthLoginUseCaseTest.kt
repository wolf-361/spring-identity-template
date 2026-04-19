package com.template.identity.application.usecase.auth

import com.template.identity.application.command.OAuthLoginCommand
import com.template.identity.application.exception.ApplicationException
import com.template.identity.application.repository.OAuthAccountRepository
import com.template.identity.application.repository.UserRepository
import com.template.identity.application.service.OAuthVerifier
import com.template.identity.application.service.TokenPairIssuer
import com.template.identity.buildAuthResult
import com.template.identity.buildOAuthAccount
import com.template.identity.buildOAuthUserInfo
import com.template.identity.buildUser
import com.template.identity.domain.model.OAuthProvider
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class OAuthLoginUseCaseTest {
    private val oauthVerifier: OAuthVerifier = mockk()
    private val userRepository: UserRepository = mockk()
    private val oauthAccountRepository: OAuthAccountRepository = mockk()
    private val tokenPairIssuer: TokenPairIssuer = mockk()
    private val useCase = OAuthLoginUseCase(oauthVerifier, userRepository, oauthAccountRepository, tokenPairIssuer)

    private val command = OAuthLoginCommand(provider = OAuthProvider.GOOGLE, idToken = "google-id-token")

    @Test
    fun `should authenticate and update provider email when OAuth account already exists`() {
        // Arrange
        val user = buildUser()
        val oauthAccount = buildOAuthAccount(user = user, providerEmail = "old@gmail.com")
        val userInfo = buildOAuthUserInfo(email = "new@gmail.com")
        every { oauthVerifier.verify(OAuthProvider.GOOGLE, command.idToken) } returns userInfo
        every {
            oauthAccountRepository.findByProviderAndProviderUserId(
                OAuthProvider.GOOGLE,
                userInfo.providerUserId
            )
        } returns
            oauthAccount
        every { oauthAccountRepository.save(oauthAccount) } returns oauthAccount
        every { tokenPairIssuer.issue(user, any()) } returns buildAuthResult(user)

        // Act
        val result = useCase.execute(command)

        // Assert
        result.user.id shouldBe user.id
        oauthAccount.providerEmail shouldBe "new@gmail.com"
        verify(exactly = 1) { oauthAccountRepository.save(oauthAccount) }
    }

    @Test
    fun `should link new OAuth account when email matches an existing user`() {
        // Arrange
        val existingUser = buildUser(email = "user@gmail.com")
        val userInfo = buildOAuthUserInfo(email = existingUser.email)
        every { oauthVerifier.verify(OAuthProvider.GOOGLE, command.idToken) } returns userInfo
        every { oauthAccountRepository.findByProviderAndProviderUserId(any(), any()) } returns null
        every { userRepository.findByEmail(userInfo.email) } returns existingUser
        every { oauthAccountRepository.findByUserAndProvider(existingUser, OAuthProvider.GOOGLE) } returns null
        every { oauthAccountRepository.save(any()) } answers { firstArg() }
        every { tokenPairIssuer.issue(existingUser, any()) } returns buildAuthResult(existingUser)

        // Act
        val result = useCase.execute(command)

        // Assert
        result.user.id shouldBe existingUser.id
        verify(exactly = 1) { oauthAccountRepository.save(any()) }
        verify(exactly = 0) { userRepository.save(any()) }
    }

    @Test
    fun `should create new user and OAuth account when neither exists`() {
        // Arrange
        val userInfo = buildOAuthUserInfo()
        val newUser = buildUser(email = userInfo.email)
        every { oauthVerifier.verify(OAuthProvider.GOOGLE, command.idToken) } returns userInfo
        every { oauthAccountRepository.findByProviderAndProviderUserId(any(), any()) } returns null
        every { userRepository.findByEmail(userInfo.email) } returns null
        every { userRepository.save(any()) } returns newUser
        every { oauthAccountRepository.save(any()) } answers { firstArg() }
        every { tokenPairIssuer.issue(newUser, any()) } returns buildAuthResult(newUser)

        // Act
        val result = useCase.execute(command)

        // Assert
        result.user.id shouldBe newUser.id
        verify(exactly = 1) { userRepository.save(any()) }
        verify(exactly = 1) { oauthAccountRepository.save(any()) }
    }

    @Test
    fun `should throw InvalidCredentials when resolved user is inactive`() {
        // Arrange
        val inactiveUser = buildUser(isActive = false)
        val oauthAccount = buildOAuthAccount(user = inactiveUser)
        val userInfo = buildOAuthUserInfo()
        every { oauthVerifier.verify(OAuthProvider.GOOGLE, command.idToken) } returns userInfo
        every { oauthAccountRepository.findByProviderAndProviderUserId(any(), any()) } returns oauthAccount
        every { oauthAccountRepository.save(any()) } answers { firstArg() }

        // Act & Assert
        shouldThrow<ApplicationException.InvalidCredentials> {
            useCase.execute(command)
        }
        verify(exactly = 0) { tokenPairIssuer.issue(any(), any()) }
    }

    @Test
    fun `should throw OAuthAccountAlreadyLinked when provider account belongs to a different user`() {
        // Arrange
        val existingUser = buildUser()
        val userInfo = buildOAuthUserInfo(email = existingUser.email)
        val linkedAccount = buildOAuthAccount(user = buildUser()) // linked to a different user
        every { oauthVerifier.verify(OAuthProvider.GOOGLE, command.idToken) } returns userInfo
        every { oauthAccountRepository.findByProviderAndProviderUserId(any(), any()) } returns null
        every { userRepository.findByEmail(userInfo.email) } returns existingUser
        every { oauthAccountRepository.findByUserAndProvider(existingUser, OAuthProvider.GOOGLE) } returns linkedAccount

        // Act & Assert
        shouldThrow<ApplicationException.OAuthAccountAlreadyLinked> {
            useCase.execute(command)
        }
    }
}