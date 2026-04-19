package com.template.identity.infrastructure.security

import com.template.identity.application.service.PasswordEncoder
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service

@Service
class PasswordEncoderImpl : PasswordEncoder {

    private val bcrypt = BCryptPasswordEncoder()

    override fun encode(rawPassword: String): String = bcrypt.encode(rawPassword)!!

    override fun matches(rawPassword: String, encodedPassword: String): Boolean =
        bcrypt.matches(rawPassword, encodedPassword)
}
