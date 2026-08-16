package `in`.over.demo.application.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class EmployeeWriteDTO(
    @field:NotBlank
    @field:Size(max = 100)
    val lastName: String,
    @field:NotBlank
    @field:Size(max = 100)
    val firstName: String,
    @field:Size(max = 100)
    val middleName: String? = null,
)
