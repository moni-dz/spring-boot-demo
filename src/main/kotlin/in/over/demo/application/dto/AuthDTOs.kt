package `in`.over.demo.application.dto

import jakarta.validation.constraints.NotBlank

data class LoginRequestDTO(
    @field:NotBlank
    val username: String,
    @field:NotBlank
    val password: String,
)
