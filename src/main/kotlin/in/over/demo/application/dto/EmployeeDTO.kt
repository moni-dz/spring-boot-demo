package `in`.over.demo.application.dto

data class EmployeeDTO(
    val id: Long,
    val lastName: String,
    val firstName: String,
    val middleName: String?,
    val username: String,
    val email: String,
    val roles: List<RoleDTO>,
)
