package com.template.identity.infrastructure.web.mapper

import com.template.identity.application.result.PublicUserResult
import com.template.identity.application.result.UserResult
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class UserWebMapperTest {
    private val id = UUID.randomUUID()
    private val now = Instant.now()

    private val userResult =
        UserResult(
            id = id,
            email = "user@example.com",
            firstName = "Jane",
            lastName = "Doe",
            isActive = true,
            createdAt = now,
            updatedAt = now
        )

    @Test
    fun `toResponse should map all fields from UserResult`() {
        val response = UserWebMapper.toResponse(userResult)

        response.id shouldBe id
        response.email shouldBe "user@example.com"
        response.firstName shouldBe "Jane"
        response.lastName shouldBe "Doe"
        response.isActive shouldBe true
        response.createdAt shouldBe now
        response.updatedAt shouldBe now
    }

    @Test
    fun `toPublicResponse should expose only id and name — not email or status`() {
        val result = PublicUserResult(id = id, firstName = "Jane", lastName = "Doe")

        val response = UserWebMapper.toPublicResponse(result)

        response.id shouldBe id
        response.firstName shouldBe "Jane"
        response.lastName shouldBe "Doe"
        // PublicUserResponse intentionally has no email or isActive field
        response::class.members.map { it.name } shouldNotBe listOf("email", "isActive")
    }
}