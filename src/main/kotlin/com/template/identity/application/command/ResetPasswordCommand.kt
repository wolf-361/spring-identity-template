package com.template.identity.application.command

data class ResetPasswordCommand(
    val rawToken: String,
    val newPassword: String,
)
