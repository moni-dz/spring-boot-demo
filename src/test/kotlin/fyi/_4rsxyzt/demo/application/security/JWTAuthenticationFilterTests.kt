package fyi._4rsxyzt.demo.application.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import jakarta.servlet.FilterChain
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.security.core.context.SecurityContextHolder
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class JWTAuthenticationFilterTests {
    private val secret = "test-secret-1234567890"
    private val jwtService = JWTService(secret, 60)
    private val filter = JWTAuthenticationFilter(jwtService, "auth_token")

    @AfterEach
    fun clearContext() = SecurityContextHolder.clearContext()

    @Test
    fun `token without roles claim authenticates with no authorities instead of throwing`() {
        val token = JWT.create()
            .withSubject("bob")
            .withClaim("employeeId", 1L)
            .sign(Algorithm.HMAC256(secret))

        filter.doFilter(requestWithCookie(token), mock(HttpServletResponse::class.java), mock(FilterChain::class.java))

        val auth = SecurityContextHolder.getContext().authentication
        assertNotNull(auth)
        assertTrue(auth.authorities.isEmpty())
    }

    @Test
    fun `token with roles claim authenticates with matching authorities`() {
        val token = JWT.create()
            .withSubject("bob")
            .withClaim("employeeId", 1L)
            .withClaim("roles", listOf("ADMIN"))
            .sign(Algorithm.HMAC256(secret))

        filter.doFilter(requestWithCookie(token), mock(HttpServletResponse::class.java), mock(FilterChain::class.java))

        val auth = SecurityContextHolder.getContext().authentication
        assertNotNull(auth)
        assertTrue(auth.authorities.any { it.authority == "ROLE_ADMIN" })
    }

    private fun requestWithCookie(token: String): HttpServletRequest {
        val request = mock(HttpServletRequest::class.java)
        `when`(request.cookies).thenReturn(arrayOf(Cookie("auth_token", token)))
        return request
    }
}
