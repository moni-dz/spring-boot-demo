package fyi._4rsxyzt.demo.application.controller

import fyi._4rsxyzt.demo.application.dto.LoginRequestDTO
import fyi._4rsxyzt.demo.application.security.EmployeeUserDetails
import fyi._4rsxyzt.demo.application.security.JWTService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.AuthenticationException
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth")
class AuthController(
    private val authenticationManager: AuthenticationManager,
    private val jwtService: JWTService,
    @Value($$"${jwt.cookie-name}") private val cookieName: String,
    @Value($$"${jwt.expiration-minutes}") private val expirationMinutes: Long,
) {
    @PostMapping("/login")
    @Operation(
        summary = "Authenticate and receive an auth cookie",
        description = "Sets an httpOnly, secure, SameSite=Strict auth_token cookie.",
    )
    @ApiResponse(responseCode = "401", description = "Bad credentials")
    fun login(@Valid @RequestBody request: LoginRequestDTO, response: HttpServletResponse): ResponseEntity<Void> {
        val employee = try {
            val auth = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(request.username, request.password),
            )

            (auth.principal as EmployeeUserDetails).employee
        } catch (_: AuthenticationException) {
            return ResponseEntity.status(401).build()
        }

        val token = jwtService.generate(employee.id, employee.username, employee.roles.map { it.name })
        response.addHeader("Set-Cookie", cookie(token, expirationMinutes * 60).toString())
        return ResponseEntity.ok().build()
    }

    @PostMapping("/logout")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Clear the auth cookie", description = "Requires authentication.")
    fun logout(response: HttpServletResponse): ResponseEntity<Void> {
        response.addHeader("Set-Cookie", cookie("", 0).toString())
        return ResponseEntity.ok().build()
    }

    private fun cookie(value: String, maxAgeSeconds: Long) = ResponseCookie.from(cookieName, value)
        .httpOnly(true)
        .secure(true)
        .sameSite("Strict")
        .path("/")
        .maxAge(maxAgeSeconds)
        .build()
}
