package fyi._4rsxyzt.demo.application.security

import fyi._4rsxyzt.demo.domain.model.Employee
import fyi._4rsxyzt.demo.domain.repository.EmployeeRepository
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

class EmployeeUserDetails(val employee: Employee) : UserDetails {
    override fun getAuthorities(): Collection<GrantedAuthority> =
        employee.roles.map { SimpleGrantedAuthority("ROLE_${it.name}") }

    override fun getPassword(): String = employee.passwordHash
    override fun getUsername(): String = employee.username
}

@Service
class EmployeeUserDetailsService(
    private val employeeRepository: EmployeeRepository,
) : UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails =
        employeeRepository.findByUsername(username)?.let(::EmployeeUserDetails)
            ?: throw UsernameNotFoundException(username)
}
