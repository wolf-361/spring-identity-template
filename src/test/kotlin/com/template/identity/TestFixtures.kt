package com.template.identity

import com.template.identity.application.result.AuthenticationResult
import com.template.identity.application.result.OAuthUserInfo
import com.template.identity.domain.model.OAuthAccount
import com.template.identity.domain.model.OAuthProvider
import com.template.identity.domain.model.PasswordResetToken
import com.template.identity.domain.model.RefreshToken
import com.template.identity.domain.model.User
import java.time.Instant
import java.util.UUID

fun buildUser(
    id: UUID = UUID.randomUUID(),
    email: String = "user@example.com",
    password: String? = "hashed-password",
    firstName: String = "Jane",
    lastName: String = "Doe",
    isActive: Boolean = true,
) = User(
    id = id,
    email = email,
    password = password,
    firstName = firstName,
    lastName = lastName,
    isActive = isActive,
)

fun buildRefreshToken(
    user: User,
    id: UUID = UUID.randomUUID(),
    tokenHash: String = "token-hash",
    familyId: UUID = UUID.randomUUID(),
    expiresAt: Instant = Instant.now().plusSeconds(86_400),
    revokedAt: Instant? = null,
) = RefreshToken(
    id = id,
    user = user,
    tokenHash = tokenHash,
    familyId = familyId,
    expiresAt = expiresAt,
    revokedAt = revokedAt,
)

fun buildPasswordResetToken(
    user: User,
    id: UUID = UUID.randomUUID(),
    tokenHash: String = "reset-token-hash",
    expiresAt: Instant = Instant.now().plusSeconds(3_600),
) = PasswordResetToken(
    id = id,
    user = user,
    tokenHash = tokenHash,
    expiresAt = expiresAt,
)

fun buildOAuthAccount(
    user: User,
    id: UUID = UUID.randomUUID(),
    provider: OAuthProvider = OAuthProvider.GOOGLE,
    providerUserId: String = "google-sub-123",
    providerEmail: String = "user@gmail.com",
) = OAuthAccount(
    id = id,
    user = user,
    provider = provider,
    providerUserId = providerUserId,
    providerEmail = providerEmail,
)

fun buildOAuthUserInfo(
    providerUserId: String = "google-sub-123",
    email: String = "user@gmail.com",
    firstName: String = "Jane",
    lastName: String = "Doe",
) = OAuthUserInfo(
    providerUserId = providerUserId,
    email = email,
    firstName = firstName,
    lastName = lastName,
)

fun buildAuthResult(user: User) = AuthenticationResult(
    accessToken = "access-token",
    refreshToken = "refresh-token",
    user = user,
)
