package com.template.identity.application.command

data class LoginCommand(
    val email: String,
    val password: String,
)
