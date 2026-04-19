package com.template.identity.infrastructure.web.exception

import java.time.Instant

data class ErrorResponse(
    val code: String,
    val message: String,
    val timestamp: Instant,
    val correlationId: String?
)