package `in`.over.demo.application.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class EmployeeWriteDTO(
    val lastName: String,
    val firstName: String,
    val middleName: String? = null,
    @field:NotBlank
    val username: String = "",
    @field:Email
    @field:NotBlank
    val email: String = "",
    val password: String? = null,
    val roleIds: Set<Long> = emptySet(),
)
