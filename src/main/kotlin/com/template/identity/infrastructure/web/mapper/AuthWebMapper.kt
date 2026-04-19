package com.template.identity.infrastructure.web.mapper

import com.template.identity.application.command.ForgotPasswordCommand
import com.template.identity.application.command.LoginCommand
import com.template.identity.application.command.LogoutCommand
import com.template.identity.application.command.OAuthLoginCommand
import com.template.identity.application.command.RefreshTokensCommand
import com.template.identity.application.command.RegisterUserCommand
import com.template.identity.application.command.ResetPasswordCommand
import com.template.identity.application.result.AuthenticationResult
import com.template.identity.domain.model.OAuthProvider
import com.template.identity.infrastructure.web.dto.request.ForgotPasswordRequest
import com.template.identity.infrastructure.web.dto.request.LoginRequest
import com.template.identity.infrastructure.web.dto.request.LogoutRequest
import com.template.identity.infrastructure.web.dto.request.OAuthLoginRequest
import com.template.identity.infrastructure.web.dto.request.RefreshTokenRequest
import com.template.identity.infrastructure.web.dto.request.RegisterRequest
import com.template.identity.infrastructure.web.dto.request.ResetPasswordRequest
import com.template.identity.infrastructure.web.dto.response.AuthResponse
import com.template.identity.infrastructure.web.dto.response.UserResponse

object AuthWebMapper {
    fun toCommand(request: LoginRequest) =
        LoginCommand(
            email = request.email,
            password = request.password
        )

    fun toCommand(request: RegisterRequest) =
        RegisterUserCommand(
            email = request.email,
            password = request.password,
            firstName = request.firstName,
            lastName = request.lastName
        )

    fun toCommand(request: OAuthLoginRequest) =
        OAuthLoginCommand(
            provider = OAuthProvider.fromString(request.provider),
            idToken = request.idToken
        )

    fun toCommand(request: RefreshTokenRequest) =
        RefreshTokensCommand(
            rawRefreshToken = request.refreshToken
        )

    fun toCommand(request: LogoutRequest) =
        LogoutCommand(
            rawRefreshToken = request.refreshToken
        )

    fun toCommand(
        request: ForgotPasswordRequest,
        frontendUrl: String
    ) = ForgotPasswordCommand(
        email = request.email,
        frontendUrl = frontendUrl
    )

    fun toCommand(request: ResetPasswordRequest) =
        ResetPasswordCommand(
            rawToken = request.token,
            newPassword = request.newPassword
        )

    fun toResponse(result: AuthenticationResult): AuthResponse {
        val user = result.user
        return AuthResponse(
            accessToken = result.accessToken,
            refreshToken = result.refreshToken,
            user =
                UserResponse(
                    id = user.id!!,
                    email = user.email,
                    firstName = user.firstName,
                    lastName = user.lastName,
                    isActive = user.isActive,
                    createdAt = user.createdAt,
                    updatedAt = user.updatedAt
                )
        )
    }
}