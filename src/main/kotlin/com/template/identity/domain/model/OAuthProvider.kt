package com.template.identity.domain.model

/** Supported OAuth providers. Add a new entry here and implement a matching verifier in infrastructure. */
enum class OAuthProvider {
    GOOGLE,
    GITHUB,
    APPLE,
    MICROSOFT;

    companion object {
        /**
         * Returns the provider matching [value] (case-insensitive).
         * @throws IllegalArgumentException if [value] does not match any known provider.
         */
        fun fromString(value: String): OAuthProvider =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unknown OAuth provider: $value")
    }
}
