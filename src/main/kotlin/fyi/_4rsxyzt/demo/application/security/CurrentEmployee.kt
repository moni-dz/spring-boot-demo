package fyi._4rsxyzt.demo.application.security

import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.context.SecurityContextHolder

data class AuthenticatedEmployee(val id: Long, val username: String)

object CurrentEmployee {
    fun get(): AuthenticatedEmployee? =
        SecurityContextHolder.getContext().authentication?.principal as? AuthenticatedEmployee

    fun isAdmin(): Boolean =
        SecurityContextHolder.getContext().authentication?.authorities
            ?.any { it.authority == "ROLE_ADMIN" } == true

    fun requireSelfOrAdmin(employeeId: Long, resource: String = "record") {
        if (!isAdmin() && get()?.id != employeeId) {
            throw AccessDeniedException("not allowed to modify another employee's $resource")
        }
    }
}
