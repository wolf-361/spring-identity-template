package com.template.identity.application.usecase.user

import com.template.identity.application.command.UpdateCurrentUserCommand
import com.template.identity.application.exception.ApplicationException
import com.template.identity.application.repository.UserRepository
import com.template.identity.buildUser
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.util.UUID

class UpdateCurrentUserUseCaseTest {

    private val userRepository: UserRepository = mockk()
    private val useCase = UpdateCurrentUserUseCase(userRepository)

    @Test
    fun `should update first name when provided`() {
        // Arrange
        val user = buildUser(firstName = "Old")
        every { userRepository.findById(user.id!!) } returns user
        every { userRepository.save(any()) } answers { firstArg() }

        // Act
        val result = useCase.execute(UpdateCurrentUserCommand(userId = user.id!!, firstName = "New"))

        // Assert
        result.firstName shouldBe "New"
    }

    @Test
    fun `should update last name when provided`() {
        // Arrange
        val user = buildUser(lastName = "Old")
        every { userRepository.findById(user.id!!) } returns user
        every { userRepository.save(any()) } answers { firstArg() }

        // Act
        val result = useCase.execute(UpdateCurrentUserCommand(userId = user.id!!, lastName = "New"))

        // Assert
        result.lastName shouldBe "New"
    }

    @Test
    fun `should update email when new email is not taken`() {
        // Arrange
        val user = buildUser(email = "old@example.com")
        every { userRepository.findById(user.id!!) } returns user
        every { userRepository.existsByEmail("new@example.com") } returns false
        every { userRepository.save(any()) } answers { firstArg() }

        // Act
        val result = useCase.execute(UpdateCurrentUserCommand(userId = user.id!!, email = "new@example.com"))

        // Assert
        result.email shouldBe "new@example.com"
    }

    @Test
    fun `should not check email uniqueness when email is unchanged`() {
        // Arrange
        val user = buildUser(email = "same@example.com")
        every { userRepository.findById(user.id!!) } returns user
        every { userRepository.save(any()) } answers { firstArg() }

        // Act
        useCase.execute(UpdateCurrentUserCommand(userId = user.id!!, email = "same@example.com"))

        // Assert
        verify(exactly = 0) { userRepository.existsByEmail(any()) }
    }

    @Test
    fun `should throw EmailAlreadyExists when new email is already taken`() {
        // Arrange
        val user = buildUser(email = "old@example.com")
        every { userRepository.findById(user.id!!) } returns user
        every { userRepository.existsByEmail("taken@example.com") } returns true

        // Act & Assert
        shouldThrow<ApplicationException.EmailAlreadyExists> {
            useCase.execute(UpdateCurrentUserCommand(userId = user.id!!, email = "taken@example.com"))
        }
        verify(exactly = 0) { userRepository.save(any()) }
    }

    @Test
    fun `should throw UserNotFound when user does not exist`() {
        // Arrange
        every { userRepository.findById(any()) } returns null

        // Act & Assert
        shouldThrow<ApplicationException.UserNotFound> {
            useCase.execute(UpdateCurrentUserCommand(userId = UUID.randomUUID(), firstName = "Jane"))
        }
    }
}
