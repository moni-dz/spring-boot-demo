package `in`.over.demo.application.dto


data class EmployeeWriteDTO(
    val lastName: String,
    val firstName: String,
    val middleName: String? = null,
)
