package com.template.identity.application.command

data class RegisterUserCommand(
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String,
)
