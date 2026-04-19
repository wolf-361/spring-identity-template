package com.template.identity.infrastructure.external.email

import com.template.identity.application.service.EmailSender
import com.template.identity.infrastructure.config.AppProperties
import jakarta.mail.internet.MimeMessage
import org.slf4j.LoggerFactory
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service

@Service
class BrevoEmailSender(
    private val mailSender: JavaMailSender,
    private val appProperties: AppProperties
) : EmailSender {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun sendPasswordResetEmail(
        to: String,
        firstName: String,
        resetLink: String
    ) {
        sendHtmlEmail(
            to = to,
            subject = "Reset your password",
            htmlContent = buildResetBody(firstName, resetLink)
        )
    }

    private fun sendHtmlEmail(
        to: String,
        subject: String,
        htmlContent: String
    ) {
        log.info("Preparing to send email to: {} with subject: '{}'", to, subject)
        try {
            val message: MimeMessage = mailSender.createMimeMessage()
            val helper = MimeMessageHelper(message, true, "UTF-8")
            helper.setFrom(appProperties.email.fromAddress)
            helper.setTo(to)
            helper.setSubject(subject)
            helper.setText(htmlContent, true)
            mailSender.send(message)
            log.info("Email successfully sent to: {}", to)
        } catch (e: Exception) {
            log.error("Failed to send email to: {}. Reason: {}", to, e.message)
            throw e
        }
    }

    private fun buildResetBody(
        firstName: String,
        resetLink: String
    ) = """
        <!DOCTYPE html>
        <html>
        <body>
          <p>Hi $firstName,</p>
          <p>Click the link below to reset your password. The link expires in <strong>1 hour</strong>.</p>
          <p><a href="$resetLink">Reset password</a></p>
          <p>If you didn't request this, you can safely ignore this email.</p>
        </body>
        </html>
        """.trimIndent()
}