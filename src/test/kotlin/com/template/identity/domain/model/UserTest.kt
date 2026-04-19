package com.template.identity.domain.model

import com.template.identity.buildUser
import com.template.identity.domain.exception.DomainException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import org.junit.jupiter.api.Test

class UserTest {
    @Test
    fun `hasPassword should return true when password is set`() {
        buildUser(password = "hashed").hasPassword().shouldBeTrue()
    }

    @Test
    fun `hasPassword should return false when password is null`() {
        buildUser(password = null).hasPassword().shouldBeFalse()
    }

    @Test
    fun `requireCanRemoveAuthMethod should not throw when user has a password`() {
        buildUser(password = "hashed").requireCanRemoveAuthMethod(remainingOAuthAccountCount = 0)
    }

    @Test
    fun `requireCanRemoveAuthMethod should not throw when oauth accounts remain`() {
        buildUser(password = null).requireCanRemoveAuthMethod(remainingOAuthAccountCount = 1)
    }

    @Test
    fun `requireCanRemoveAuthMethod should throw when no password and no oauth accounts remain`() {
        shouldThrow<DomainException.LastAuthMethodCannotBeRemoved> {
            buildUser(password = null).requireCanRemoveAuthMethod(remainingOAuthAccountCount = 0)
        }
    }
}