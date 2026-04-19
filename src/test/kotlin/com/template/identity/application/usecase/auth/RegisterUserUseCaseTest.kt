package com.template.identity.application.usecase.auth

import com.template.identity.application.command.RegisterUserCommand
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

class RegisterUserUseCaseTest {
    private val userRepository: UserRepository = mockk()
    private val passwordEncoder: PasswordEncoder = mockk()
    private val tokenPairIssuer: TokenPairIssuer = mockk()
    private val useCase = RegisterUserUseCase(userRepository, passwordEncoder, tokenPairIssuer)

    @Test
    fun `should create user and return authentication result when email is available`() {
        // Arrange
        val command =
            RegisterUserCommand(
                email = "new@example.com",
                password = "password123",
                firstName = "Jane",
                lastName = "Doe"
            )
        every { userRepository.existsByEmail(command.email) } returns false
        every { passwordEncoder.encode(command.password) } returns "hashed-password"
        every { userRepository.save(any()) } answers { firstArg() }
        every { tokenPairIssuer.issue(any(), any()) } returns buildAuthResult(buildUser(email = command.email))

        // Act
        val result = useCase.execute(command)

        // Assert
        result.user.email shouldBe command.email
        verify(exactly = 1) {
            userRepository.save(match { it.email == command.email && it.password == "hashed-password" })
        }
    }

    @Test
    fun `should throw EmailAlreadyExists when email is already registered`() {
        // Arrange
        val command =
            RegisterUserCommand(
                email = "taken@example.com",
                password = "password123",
                firstName = "Jane",
                lastName = "Doe"
            )
        every { userRepository.existsByEmail(command.email) } returns true

        // Act & Assert
        shouldThrow<ApplicationException.EmailAlreadyExists> {
            useCase.execute(command)
        }
        verify(exactly = 0) { userRepository.save(any()) }
    }
}