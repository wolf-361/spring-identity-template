package com.template.identity.application

import java.security.MessageDigest
import java.security.SecureRandom

internal fun sha256(input: String): String =
    MessageDigest
        .getInstance("SHA-256")
        .digest(input.toByteArray())
        .joinToString("") { "%02x".format(it) }

private val secureRandom = SecureRandom()

internal fun generateSecureToken(): String {
    val bytes = ByteArray(32)
    secureRandom.nextBytes(bytes)
    return bytes.joinToString("") { "%02x".format(it) }
}