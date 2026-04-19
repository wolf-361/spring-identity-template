package com.template.identity.application.repository

import com.template.identity.domain.model.User
import java.util.UUID

/** Persistence contract for [User] entities. */
interface UserRepository {
    /** Returns the user with [id], or null if not found. */
    fun findById(id: UUID): User?

    /** Returns the user with [email], or null if not found. */
    fun findByEmail(email: String): User?

    /** Returns true if a user with [email] already exists. */
    fun existsByEmail(email: String): Boolean

    /** Persists [user] and returns the saved instance. */
    fun save(user: User): User
}