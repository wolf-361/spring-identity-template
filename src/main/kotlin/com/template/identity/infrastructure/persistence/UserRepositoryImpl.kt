package com.template.identity.infrastructure.persistence

import com.template.identity.application.repository.UserRepository
import com.template.identity.domain.model.User
import com.template.identity.infrastructure.persistence.jpa.JpaUserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class UserRepositoryImpl(
    private val jpa: JpaUserRepository,
) : UserRepository {

    override fun findById(id: UUID): User? = jpa.findByIdOrNull(id)

    override fun findByEmail(email: String): User? = jpa.findByEmail(email)

    override fun existsByEmail(email: String): Boolean = jpa.existsByEmail(email)

    override fun save(user: User): User = jpa.save(user)
}
