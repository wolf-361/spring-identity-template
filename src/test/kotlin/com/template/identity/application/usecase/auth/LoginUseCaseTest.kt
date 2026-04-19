package com.template.identity.application.usecase.auth

import com.template.identity.application.command.LoginCommand
import com.template.identity.application.exception.ApplicationException
import com.template.identity.application.repository.UserRepository
import com.template.identity.application.service.PasswordEncoder
import com.template.identity.application.service.TokenPairIssuer
import com.template.identity.buildAuthResult
import com.template.identity.buildUser
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class LoginUseCaseTest {

    private val userRepository: UserRepository = mockk()
    private val passwordEncoder: PasswordEncoder = mockk()
    private val tokenPairIssuer: TokenPairIssuer = mockk()
    private val useCase = LoginUseCase(userRepository, passwordEncoder, tokenPairIssuer)

    @Test
    fun `should return authentication result when credentials are valid`() {
        // Arrange
        val user = buildUser()
        every { userRepository.findByEmail(user.email) } returns user
        every { passwordEncoder.matches("password", user.password!!) } returns true
        every { tokenPairIssuer.issue(user, any()) } returns buildAuthResult(user)

        // Act
        val result = useCase.execute(LoginCommand(email = user.email, password = "password"))

        // Assert
        result.user.email shouldBe user.email
        verify(exactly = 1) { tokenPairIssuer.issue(user, any()) }
    }

    @Test
    fun `should throw InvalidCredentials when user is not found`() {
        // Arrange
        every { userRepository.findByEmail(any()) } returns null

        // Act & Assert
        shouldThrow<ApplicationException.InvalidCredentials> {
            useCase.execute(LoginCommand(email = "unknown@example.com", password = "password"))
        }
    }

    @Test
    fun `should throw InvalidCredentials when account is inactive`() {
        // Arrange
        val user = buildUser(isActive = false)
        every { userRepository.findByEmail(user.email) } returns user

        // Act & Assert
        shouldThrow<ApplicationException.InvalidCredentials> {
            useCase.execute(LoginCommand(email = user.email, password = "password"))
        }
    }

    @Test
    fun `should throw InvalidCredentials when account has no password set`() {
        // Arrange
        val user = buildUser(password = null)
        every { userRepository.findByEmail(user.email) } returns user

        // Act & Assert
        shouldThrow<ApplicationException.InvalidCredentials> {
            useCase.execute(LoginCommand(email = user.email, password = "password"))
        }
    }

    @Test
    fun `should throw InvalidCredentials when password does not match`() {
        // Arrange
        val user = buildUser()
        every { userRepository.findByEmail(user.email) } returns user
        every { passwordEncoder.matches("wrong-password", user.password!!) } returns false

        // Act & Assert
        shouldThrow<ApplicationException.InvalidCredentials> {
            useCase.execute(LoginCommand(email = user.email, password = "wrong-password"))
        }
    }
}
