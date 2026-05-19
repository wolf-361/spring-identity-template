package com.template.identity.infrastructure.web.exception

import com.template.identity.application.exception.ApplicationException
import com.template.identity.domain.exception.DomainException
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.Instant

@RestControllerAdvice
class GlobalExceptionHandler {
    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(
        ApplicationException.InvalidCredentials::class,
        ApplicationException.UserInactive::class
    )
    fun handleInvalidCredentials(ex: ApplicationException) =
        error(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Invalid credentials")
            .also { log.warn("Authentication failed") }

    @ExceptionHandler(ApplicationException.EmailAlreadyExists::class)
    fun handleEmailAlreadyExists(ex: ApplicationException) =
        error(HttpStatus.CONFLICT, "REGISTRATION_FAILED", "Unable to create account")
            .also { log.warn("Registration failed") }

    @ExceptionHandler(
        ApplicationException.RefreshTokenExpired::class,
        ApplicationException.RefreshTokenInvalid::class,
        ApplicationException.RefreshTokenRevoked::class
    )
    fun handleRefreshTokenErrors(ex: ApplicationException) =
        error(HttpStatus.UNAUTHORIZED, "SESSION_EXPIRED", "Session expired, please login again")
            .also { log.warn("Refresh token error") }

    @ExceptionHandler(
        ApplicationException.PasswordResetTokenInvalid::class,
        ApplicationException.PasswordResetTokenExpired::class
    )
    fun handlePasswordResetErrors(ex: ApplicationException) =
        error(HttpStatus.BAD_REQUEST, "INVALID_RESET_TOKEN", "Invalid or expired password reset link")
            .also { log.warn("Password reset token error") }

    @ExceptionHandler(ApplicationException.UserNotFound::class)
    fun handleUserNotFound(ex: ApplicationException) =
        error(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "User not found")
            .also { log.warn("User not found") }

    @ExceptionHandler(ApplicationException.OAuthProviderNotSupported::class)
    fun handleOAuthProviderNotSupported(ex: ApplicationException) =
        error(HttpStatus.BAD_REQUEST, "OAUTH_PROVIDER_NOT_SUPPORTED", "OAuth provider not supported")
            .also { log.warn("OAuth provider not supported") }

    @ExceptionHandler(ApplicationException.OAuthAccountAlreadyLinked::class)
    fun handleOAuthAccountAlreadyLinked(ex: ApplicationException) =
        error(HttpStatus.CONFLICT, "OAUTH_ACCOUNT_ALREADY_LINKED", "OAuth account is already linked to another user")
            .also { log.warn("OAuth account already linked") }

    @ExceptionHandler(DomainException.LastAuthMethodCannotBeRemoved::class)
    fun handleLastAuthMethod(ex: DomainException) =
        error(HttpStatus.UNPROCESSABLE_ENTITY, "LAST_AUTH_METHOD", "Cannot remove the last authentication method")
            .also { log.warn("Last auth method removal attempt") }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val field = ex.bindingResult.fieldErrors.firstOrNull()
        val message = if (field != null) "${field.field}: ${field.defaultMessage}" else "Validation failed"
        return error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleNotReadable(ex: HttpMessageNotReadableException) =
        error(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "Malformed or missing request body")

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException) =
        error(HttpStatus.BAD_REQUEST, "BAD_REQUEST", ex.message ?: "Bad request")

    @ExceptionHandler(Exception::class)
    fun handleGeneric(ex: Exception): ResponseEntity<ErrorResponse> {
        log.error("Unexpected error", ex)
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "An unexpected error occurred")
    }

    private fun error(
        status: HttpStatus,
        code: String,
        message: String
    ) = ResponseEntity.status(status).body(
        ErrorResponse(
            code = code,
            message = message,
            timestamp = Instant.now(),
            correlationId = MDC.get("correlationId")
        )
    )
}