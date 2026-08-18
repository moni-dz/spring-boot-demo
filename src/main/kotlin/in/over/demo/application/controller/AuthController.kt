package `in`.over.demo.application.controller

import `in`.over.demo.application.dto.LoginRequestDTO
import `in`.over.demo.application.security.JWTService
import `in`.over.demo.domain.repository.EmployeeRepository
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth")
class AuthController(
    private val employeeRepository: EmployeeRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JWTService,
    @Value($$"${jwt.cookie-name}") private val cookieName: String,
    @Value($$"${jwt.expiration-minutes}") private val expirationMinutes: Long,
) {
    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequestDTO, response: HttpServletResponse): ResponseEntity<Void> {
        val employee = employeeRepository.findByUsername(request.username)
        if (employee == null || !passwordEncoder.matches(request.password, employee.passwordHash)) {
            return ResponseEntity.status(401).build()
        }

        val token = jwtService.generate(employee.id, employee.username, employee.roles.map { it.name })
        response.addHeader("Set-Cookie", cookie(token, expirationMinutes * 60).toString())
        return ResponseEntity.ok().build()
    }

    @PostMapping("/logout")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
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
