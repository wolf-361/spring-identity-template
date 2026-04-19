package com.template.identity.application.service

/**
 * Hashes passwords and verifies raw passwords against stored hashes.
 * Decouples use cases from the BCrypt implementation in Spring Security.
 */
interface PasswordEncoder {
    /** Returns a one-way hash of [rawPassword] suitable for persistent storage. */
    fun encode(rawPassword: String): String

    /** Returns true if [rawPassword] matches the [encodedPassword] hash. */
    fun matches(
        rawPassword: String,
        encodedPassword: String
    ): Boolean
}