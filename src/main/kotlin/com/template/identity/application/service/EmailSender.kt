package com.template.identity.application.service

/**
 * Sends transactional emails. Implementations are provider-specific (Brevo, SendGrid, etc.).
 */
interface EmailSender {
    /**
     * Sends a password reset email containing a magic link to [to].
     * [resetLink] already contains the raw token as a query parameter — never log it.
     */
    fun sendPasswordResetEmail(
        to: String,
        firstName: String,
        resetLink: String
    )
}