package com.template.identity.infrastructure.security

import com.template.identity.application.repository.UserRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import java.util.UUID
import org.springframework.security.core.userdetails.User as SpringUser

/**
 * Loads a [UserDetails] by user ID (the JWT `sub` claim).
 * Used by [com.template.identity.infrastructure.security.jwt.JwtAuthenticationFilter]
 * to populate the security context after token validation.
 */
@Service
class AppUserDetailsService(
    private val userRepository: UserRepository
) : UserDetailsService {
    override fun loadUserByUsername(userId: String): UserDetails {
        val user =
            userRepository.findById(UUID.fromString(userId))
                ?: throw UsernameNotFoundException("User not found: $userId")

        return SpringUser
            .builder()
            .username(user.id.toString())
            .password(user.password ?: "")
            .authorities(emptyList())
            .build()
    }
}