package com.template.identity.infrastructure.security.jwt

import com.template.identity.application.service.JwtService
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jakarta.servlet.FilterChain
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import java.util.UUID

class JwtAuthenticationFilterTest {
    private val jwtService = mockk<JwtService>()
    private val userDetailsService = mockk<UserDetailsService>()
    private val filter = JwtAuthenticationFilter(jwtService, userDetailsService)

    private val response = MockHttpServletResponse()
    private val chain = mockk<FilterChain>(relaxed = true)

    @BeforeEach
    fun clearSecurityContext() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `should pass through when Authorization header is absent`() {
        val request = MockHttpServletRequest()

        filter.doFilter(request, response, chain)

        SecurityContextHolder.getContext().authentication shouldBe null
        verify { chain.doFilter(request, response) }
    }

    @Test
    fun `should pass through when Authorization header is not a Bearer token`() {
        val request = MockHttpServletRequest().apply { addHeader("Authorization", "Basic dXNlcjpwYXNz") }

        filter.doFilter(request, response, chain)

        SecurityContextHolder.getContext().authentication shouldBe null
        verify { chain.doFilter(request, response) }
    }

    @Test
    fun `should authenticate user when token is valid`() {
        val userId = UUID.randomUUID()
        val userDetails = User(userId.toString(), "", emptyList())
        val request = MockHttpServletRequest().apply { addHeader("Authorization", "Bearer valid-token") }
        every { jwtService.isAccessTokenValid("valid-token") } returns true
        every { jwtService.extractUserId("valid-token") } returns userId
        every { userDetailsService.loadUserByUsername(userId.toString()) } returns userDetails

        filter.doFilter(request, response, chain)

        SecurityContextHolder.getContext().authentication shouldNotBe null
        verify { chain.doFilter(request, response) }
    }

    @Test
    fun `should proceed unauthenticated and not throw when token processing fails`() {
        val request = MockHttpServletRequest().apply { addHeader("Authorization", "Bearer bad-token") }
        every { jwtService.isAccessTokenValid("bad-token") } throws RuntimeException("invalid signature")

        filter.doFilter(request, response, chain)

        SecurityContextHolder.getContext().authentication shouldBe null
        verify { chain.doFilter(request, response) }
    }

    @Test
    fun `should skip loading user when security context already has an authenticated principal`() {
        val userId = UUID.randomUUID()
        val userDetails = User(userId.toString(), "", emptyList())
        val request = MockHttpServletRequest().apply { addHeader("Authorization", "Bearer some-token") }
        every { jwtService.isAccessTokenValid("some-token") } returns true
        every { jwtService.extractUserId("some-token") } returns userId
        every { userDetailsService.loadUserByUsername(userId.toString()) } returns userDetails

        filter.doFilter(request, response, chain)
        // Second request on same thread — context still has authentication set
        filter.doFilter(request, response, chain)

        verify(exactly = 1) { userDetailsService.loadUserByUsername(any()) }
    }
}