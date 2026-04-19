package com.template.identity.infrastructure.web.exception

import com.template.identity.application.exception.ApplicationException
import com.template.identity.domain.exception.DomainException
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import jakarta.servlet.http.HttpServletRequest
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.validation.BindingResult
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException

class GlobalExceptionHandlerTest {
    private val handler = GlobalExceptionHandler()
    private val request = mockk<HttpServletRequest>(relaxed = true)

    @Test
    fun `InvalidCredentials should return 401 INVALID_CREDENTIALS`() {
        val response = handler.handleInvalidCredentials(ApplicationException.InvalidCredentials(), request)
        response.statusCode shouldBe HttpStatus.UNAUTHORIZED
        response.body!!.code shouldBe "INVALID_CREDENTIALS"
    }

    @Test
    fun `UserInactive should return 401 INVALID_CREDENTIALS`() {
        val response = handler.handleInvalidCredentials(ApplicationException.UserInactive(), request)
        response.statusCode shouldBe HttpStatus.UNAUTHORIZED
        response.body!!.code shouldBe "INVALID_CREDENTIALS"
    }

    @Test
    fun `EmailAlreadyExists should return 409 REGISTRATION_FAILED`() {
        val response = handler.handleEmailAlreadyExists(ApplicationException.EmailAlreadyExists(), request)
        response.statusCode shouldBe HttpStatus.CONFLICT
        response.body!!.code shouldBe "REGISTRATION_FAILED"
    }

    @Test
    fun `RefreshTokenExpired should return 401 SESSION_EXPIRED`() {
        val response = handler.handleRefreshTokenErrors(ApplicationException.RefreshTokenExpired(), request)
        response.statusCode shouldBe HttpStatus.UNAUTHORIZED
        response.body!!.code shouldBe "SESSION_EXPIRED"
    }

    @Test
    fun `RefreshTokenRevoked should return 401 SESSION_EXPIRED`() {
        val response = handler.handleRefreshTokenErrors(ApplicationException.RefreshTokenRevoked(), request)
        response.statusCode shouldBe HttpStatus.UNAUTHORIZED
        response.body!!.code shouldBe "SESSION_EXPIRED"
    }

    @Test
    fun `PasswordResetTokenInvalid should return 400 INVALID_RESET_TOKEN`() {
        val response = handler.handlePasswordResetErrors(ApplicationException.PasswordResetTokenInvalid(), request)
        response.statusCode shouldBe HttpStatus.BAD_REQUEST
        response.body!!.code shouldBe "INVALID_RESET_TOKEN"
    }

    @Test
    fun `PasswordResetTokenExpired should return 400 INVALID_RESET_TOKEN`() {
        val response = handler.handlePasswordResetErrors(ApplicationException.PasswordResetTokenExpired(), request)
        response.statusCode shouldBe HttpStatus.BAD_REQUEST
        response.body!!.code shouldBe "INVALID_RESET_TOKEN"
    }

    @Test
    fun `UserNotFound should return 404 USER_NOT_FOUND`() {
        val response = handler.handleUserNotFound(ApplicationException.UserNotFound(), request)
        response.statusCode shouldBe HttpStatus.NOT_FOUND
        response.body!!.code shouldBe "USER_NOT_FOUND"
    }

    @Test
    fun `OAuthProviderNotSupported should return 400 OAUTH_PROVIDER_NOT_SUPPORTED`() {
        val response =
            handler.handleOAuthProviderNotSupported(
                ApplicationException.OAuthProviderNotSupported(),
                request
            )
        response.statusCode shouldBe HttpStatus.BAD_REQUEST
        response.body!!.code shouldBe "OAUTH_PROVIDER_NOT_SUPPORTED"
    }

    @Test
    fun `OAuthAccountAlreadyLinked should return 409 OAUTH_ACCOUNT_ALREADY_LINKED`() {
        val response =
            handler.handleOAuthAccountAlreadyLinked(
                ApplicationException.OAuthAccountAlreadyLinked(),
                request
            )
        response.statusCode shouldBe HttpStatus.CONFLICT
        response.body!!.code shouldBe "OAUTH_ACCOUNT_ALREADY_LINKED"
    }

    @Test
    fun `LastAuthMethodCannotBeRemoved should return 422 LAST_AUTH_METHOD`() {
        val response = handler.handleLastAuthMethod(DomainException.LastAuthMethodCannotBeRemoved(), request)
        response.statusCode shouldBe HttpStatus.UNPROCESSABLE_ENTITY
        response.body!!.code shouldBe "LAST_AUTH_METHOD"
    }

    @Test
    fun `validation error should return 400 VALIDATION_ERROR with field message`() {
        val fieldError = FieldError("obj", "email", "must not be blank")
        val bindingResult =
            mockk<BindingResult> {
                every { fieldErrors } returns listOf(fieldError)
            }
        val ex =
            mockk<MethodArgumentNotValidException> {
                every { this@mockk.bindingResult } returns bindingResult
            }

        val response = handler.handleValidation(ex)

        response.statusCode shouldBe HttpStatus.BAD_REQUEST
        response.body!!.code shouldBe "VALIDATION_ERROR"
        response.body!!.message shouldBe "email: must not be blank"
    }

    @Test
    fun `malformed request body should return 400 BAD_REQUEST`() {
        val response = handler.handleNotReadable(mockk(relaxed = true))
        response.statusCode shouldBe HttpStatus.BAD_REQUEST
        response.body!!.code shouldBe "BAD_REQUEST"
    }

    @Test
    fun `IllegalArgumentException should return 400 BAD_REQUEST with exception message`() {
        val response = handler.handleIllegalArgument(IllegalArgumentException("Unknown OAuth provider: facebook"))
        response.statusCode shouldBe HttpStatus.BAD_REQUEST
        response.body!!.message shouldBe "Unknown OAuth provider: facebook"
    }

    @Test
    fun `unhandled exception should return 500 INTERNAL_ERROR`() {
        val response = handler.handleGeneric(RuntimeException("unexpected"), request)
        response.statusCode shouldBe HttpStatus.INTERNAL_SERVER_ERROR
        response.body!!.code shouldBe "INTERNAL_ERROR"
    }
}