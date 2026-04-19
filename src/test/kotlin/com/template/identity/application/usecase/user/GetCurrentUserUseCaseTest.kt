package com.template.identity.application.usecase.user

import com.template.identity.application.command.GetCurrentUserCommand
import com.template.identity.application.exception.ApplicationException
import com.template.identity.application.repository.UserRepository
import com.template.identity.buildUser
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import java.util.UUID

class GetCurrentUserUseCaseTest {

    private val userRepository: UserRepository = mockk()
    private val useCase = GetCurrentUserUseCase(userRepository)

    @Test
    fun `should return user result when user exists`() {
        // Arrange
        val user = buildUser()
        every { userRepository.findById(user.id!!) } returns user

        // Act
        val result = useCase.execute(GetCurrentUserCommand(user.id!!))

        // Assert
        result.id shouldBe user.id
        result.email shouldBe user.email
        result.firstName shouldBe user.firstName
        result.lastName shouldBe user.lastName
    }

    @Test
    fun `should throw UserNotFound when user does not exist`() {
        // Arrange
        every { userRepository.findById(any()) } returns null

        // Act & Assert
        shouldThrow<ApplicationException.UserNotFound> {
            useCase.execute(GetCurrentUserCommand(UUID.randomUUID()))
        }
    }
}
