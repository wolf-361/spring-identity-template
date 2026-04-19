package com.template.identity.infrastructure.web.controller

import com.template.identity.application.usecase.auth.ForgotPasswordUseCase
import com.template.identity.application.usecase.auth.LoginUseCase
import com.template.identity.application.usecase.auth.LogoutUseCase
import com.template.identity.application.usecase.auth.OAuthLoginUseCase
import com.template.identity.application.usecase.auth.RefreshTokensUseCase
import com.template.identity.application.usecase.auth.RegisterUserUseCase
import com.template.identity.application.usecase.auth.ResetPasswordUseCase
import com.template.identity.infrastructure.config.AppProperties
import com.template.identity.infrastructure.web.dto.request.ForgotPasswordRequest
import com.template.identity.infrastructure.web.dto.request.LoginRequest
import com.template.identity.infrastructure.web.dto.request.LogoutRequest
import com.template.identity.infrastructure.web.dto.request.OAuthLoginRequest
import com.template.identity.infrastructure.web.dto.request.RefreshTokenRequest
import com.template.identity.infrastructure.web.dto.request.RegisterRequest
import com.template.identity.infrastructure.web.dto.request.ResetPasswordRequest
import com.template.identity.infrastructure.web.dto.response.AuthResponse
import com.template.identity.infrastructure.web.exception.ErrorResponse
import com.template.identity.infrastructure.web.mapper.AuthWebMapper
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth", description = "Registration, login, token management, and password reset")
class AuthController(
    private val loginUseCase: LoginUseCase,
    private val registerUserUseCase: RegisterUserUseCase,
    private val oAuthLoginUseCase: OAuthLoginUseCase,
    private val refreshTokensUseCase: RefreshTokensUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val forgotPasswordUseCase: ForgotPasswordUseCase,
    private val resetPasswordUseCase: ResetPasswordUseCase,
    private val appProperties: AppProperties,
) {

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new user account")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "Account created — token pair returned"),
        ApiResponse(responseCode = "400", description = "Validation error",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
        ApiResponse(responseCode = "409", description = "Email already in use — code: REGISTRATION_FAILED",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
    )
    fun register(@Valid @RequestBody request: RegisterRequest): AuthResponse =
        AuthWebMapper.toResponse(registerUserUseCase.execute(AuthWebMapper.toCommand(request)))

    @PostMapping("/login")
    @Operation(summary = "Authenticate with email and password")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Authenticated — token pair returned"),
        ApiResponse(responseCode = "400", description = "Validation error",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
        ApiResponse(responseCode = "401", description = "Invalid credentials — code: INVALID_CREDENTIALS",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
    )
    fun login(@Valid @RequestBody request: LoginRequest): AuthResponse =
        AuthWebMapper.toResponse(loginUseCase.execute(AuthWebMapper.toCommand(request)))

    @PostMapping("/oauth")
    @Operation(summary = "Authenticate or register via OAuth provider ID token")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Authenticated — token pair returned"),
        ApiResponse(responseCode = "400", description = "Validation error or unknown provider",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
        ApiResponse(responseCode = "401", description = "Account inactive — code: INVALID_CREDENTIALS",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
        ApiResponse(responseCode = "409", description = "OAuth account linked to different user — code: OAUTH_ACCOUNT_ALREADY_LINKED",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
    )
    fun oauthLogin(@Valid @RequestBody request: OAuthLoginRequest): AuthResponse =
        AuthWebMapper.toResponse(oAuthLoginUseCase.execute(AuthWebMapper.toCommand(request)))

    @PostMapping("/refresh")
    @Operation(summary = "Rotate a refresh token and issue a new token pair")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "New token pair issued"),
        ApiResponse(responseCode = "400", description = "Validation error",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
        ApiResponse(responseCode = "401", description = "Token invalid, expired, or reused — code: SESSION_EXPIRED",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
    )
    fun refresh(@Valid @RequestBody request: RefreshTokenRequest): AuthResponse =
        AuthWebMapper.toResponse(refreshTokensUseCase.execute(AuthWebMapper.toCommand(request)))

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Revoke the current refresh token")
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "Token revoked (also succeeds if token was already invalid)"),
        ApiResponse(responseCode = "400", description = "Validation error",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
    )
    fun logout(@Valid @RequestBody request: LogoutRequest) =
        logoutUseCase.execute(AuthWebMapper.toCommand(request))

    @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
        summary = "Request a password reset email",
        description = "Always returns 204 regardless of whether the email is registered, to prevent user enumeration."
    )
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "Reset email sent (or silently ignored if email not found)"),
        ApiResponse(responseCode = "400", description = "Validation error",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
    )
    fun forgotPassword(@Valid @RequestBody request: ForgotPasswordRequest) =
        forgotPasswordUseCase.execute(AuthWebMapper.toCommand(request, appProperties.frontendUrl))

    @PostMapping("/reset-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Reset password using a token from the reset email")
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "Password updated"),
        ApiResponse(responseCode = "400", description = "Token invalid or expired — code: INVALID_RESET_TOKEN",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
    )
    fun resetPassword(@Valid @RequestBody request: ResetPasswordRequest) =
        resetPasswordUseCase.execute(AuthWebMapper.toCommand(request))
}
