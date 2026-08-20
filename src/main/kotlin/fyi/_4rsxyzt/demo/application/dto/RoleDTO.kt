package fyi._4rsxyzt.demo.application.dto

import jakarta.validation.constraints.NotBlank

data class RoleDTO(
    val id: Long,
    val name: String,
)

data class RoleWriteDTO(
    @field:NotBlank
    val name: String,
)
