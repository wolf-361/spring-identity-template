package com.template.identity.application

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldHaveLength
import io.kotest.matchers.string.shouldMatch
import org.junit.jupiter.api.Test

class CryptoTest {
    @Test
    fun `sha256 should produce a 64-character lowercase hex string`() {
        val hash = sha256("hello")
        hash shouldHaveLength 64
        hash shouldMatch Regex("[0-9a-f]+")
    }

    @Test
    fun `sha256 should be deterministic`() {
        sha256("same-input") shouldBe sha256("same-input")
    }

    @Test
    fun `sha256 should produce different hashes for different inputs`() {
        sha256("input-a") shouldNotBe sha256("input-b")
    }

    @Test
    fun `generateSecureToken should produce a 64-character hex string`() {
        val token = generateSecureToken()
        token shouldHaveLength 64
        token shouldMatch Regex("[0-9a-f]+")
    }

    @Test
    fun `generateSecureToken should return unique values`() {
        generateSecureToken() shouldNotBe generateSecureToken()
    }
}