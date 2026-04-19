package com.template.identity.application.command

// frontendUrl is provided by the controller from config so the use case
// stays free of any infrastructure dependency for configuration.
data class ForgotPasswordCommand(
    val email: String,
    val frontendUrl: String,
)
