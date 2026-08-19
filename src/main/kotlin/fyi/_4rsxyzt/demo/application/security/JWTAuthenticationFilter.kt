package fyi._4rsxyzt.demo.application.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JWTAuthenticationFilter(
    private val jwtService: JWTService,
    @Value($$"${jwt.cookie-name}") private val cookieName: String,
) : OncePerRequestFilter() {
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        val token = request.cookies?.firstOrNull { it.name == cookieName }?.value
        val decoded = token?.let(jwtService::verify)

        if (decoded != null) {
            val authorities = decoded.getClaim("roles").asList(String::class.java)
                .map { SimpleGrantedAuthority("ROLE_$it") }
            val principal = AuthenticatedEmployee(decoded.getClaim("employeeId").asLong(), decoded.subject)

            SecurityContextHolder.getContext().authentication =
                UsernamePasswordAuthenticationToken(principal, null, authorities)
        }

        chain.doFilter(request, response)
    }
}
