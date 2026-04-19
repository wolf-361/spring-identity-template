package com.template.identity.infrastructure.security.jwt

import com.template.identity.application.service.JwtService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService,
    private val userDetailsService: UserDetailsService,
) : OncePerRequestFilter() {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val authHeader = request.getHeader("Authorization")

        // If there's no token, just pass the request along
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }

        try {
            val token = authHeader.substring(7)

            if (jwtService.isAccessTokenValid(token) &&
                SecurityContextHolder.getContext().authentication == null
            ) {
                val userId = jwtService.extractUserId(token)
                val userDetails = userDetailsService.loadUserByUsername(userId.toString())

                val auth = UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.authorities
                )
                auth.details = WebAuthenticationDetailsSource().buildDetails(request)
                SecurityContextHolder.getContext().authentication = auth

                MDC.put("userId", userId.toString())
            }
        } catch (e: Exception) {
            log.warn("JWT processing failed: ${e.javaClass.simpleName} — proceeding unauthenticated")
        }

        try {
            filterChain.doFilter(request, response)
        } finally {
            MDC.remove("userId")
        }
    }
}
