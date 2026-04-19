package com.template.identity.infrastructure.web.controller

import com.template.identity.application.command.GetCurrentUserCommand
import com.template.identity.application.usecase.user.GetCurrentUserUseCase
import com.template.identity.application.usecase.user.GetPublicUserUseCase
import com.template.identity.application.usecase.user.UpdateCurrentUserUseCase
import com.template.identity.infrastructure.web.dto.request.UpdateCurrentUserRequest
import com.template.identity.infrastructure.web.dto.response.PublicUserResponse
import com.template.identity.infrastructure.web.dto.response.UserResponse
import com.template.identity.infrastructure.web.exception.ErrorResponse
import com.template.identity.infrastructure.web.mapper.UserWebMapper
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/users")
@Tag(name = "Users", description = "User profile management")
class UserController(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val updateCurrentUserUseCase: UpdateCurrentUserUseCase,
    private val getPublicUserUseCase: GetPublicUserUseCase,
) {

    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get the authenticated user's full profile")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Profile returned"),
        ApiResponse(responseCode = "401", description = "Missing or invalid access token",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
    )
    fun getCurrentUser(): UserResponse {
        val result = getCurrentUserUseCase.execute(GetCurrentUserCommand(currentUserId()))
        return UserWebMapper.toResponse(result)
    }

    @PatchMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Update the authenticated user's profile",
        description = "All fields are optional — only provided (non-null) fields are applied."
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Updated profile returned"),
        ApiResponse(responseCode = "400", description = "Validation error",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
        ApiResponse(responseCode = "401", description = "Missing or invalid access token",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
        ApiResponse(responseCode = "409", description = "Email already in use — code: REGISTRATION_FAILED",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
    )
    fun updateCurrentUser(@Valid @RequestBody request: UpdateCurrentUserRequest): UserResponse {
        val command = UserWebMapper.toCommand(request, currentUserId())
        val result = updateCurrentUserUseCase.execute(command)
        return UserWebMapper.toResponse(result)
    }

    @GetMapping("/{id}/public")
    @Operation(summary = "Get a user's public profile by ID")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Public profile returned"),
        ApiResponse(responseCode = "404", description = "User not found — code: USER_NOT_FOUND",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
    )
    fun getPublicUser(@PathVariable id: UUID): PublicUserResponse {
        val result = getPublicUserUseCase.execute(id)
        return UserWebMapper.toPublicResponse(result)
    }

    private fun currentUserId(): UUID {
        val auth = SecurityContextHolder.getContext().authentication
            ?: error("Authenticated endpoint reached with no security context — check SecurityConfig")
        val principal = auth.principal as UserDetails
        return UUID.fromString(principal.username)
    }
}
