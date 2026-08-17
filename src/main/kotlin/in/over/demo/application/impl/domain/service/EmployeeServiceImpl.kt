package `in`.over.demo.application.impl.domain.service

import `in`.over.demo.application.dto.EmployeeWriteDTO
import `in`.over.demo.application.mapper.EmployeeMapper
import `in`.over.demo.application.security.CurrentEmployee
import `in`.over.demo.domain.model.Employee
import `in`.over.demo.domain.model.Role
import `in`.over.demo.domain.repository.EmployeeRepository
import `in`.over.demo.domain.repository.RoleRepository
import `in`.over.demo.domain.service.EmployeeService
import org.springframework.data.domain.Sort
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class EmployeeServiceImpl(
    private val repository: EmployeeRepository,
    private val roleRepository: RoleRepository,
    private val mapper: EmployeeMapper,
    private val passwordEncoder: PasswordEncoder,
) : EmployeeService {
    override fun getEmployees(): List<Employee> = repository.findAll(Sort.by("id"))

    override fun getEmployee(id: Long): Employee? = repository.findById(id).orElse(null)

    override fun create(request: EmployeeWriteDTO): Employee {
        val normalized = normalize(request)
        val password = normalized.password?.takeIf { it.isNotBlank() } ?: throw IllegalArgumentException("password is required")

        val employee = mapper.toModel(normalized)
        employee.passwordHash = passwordEncoder.encode(password)!!
        employee.roles = resolveRoles(normalized.roleIds)

        return repository.save(employee)
    }

    override fun update(id: Long, request: EmployeeWriteDTO): Employee? {
        CurrentEmployee.requireSelfOrAdmin(id)
        val employee = getEmployee(id) ?: return null
        val normalized = normalize(request)

        mapper.update(normalized, employee)
        normalized.password?.takeIf { it.isNotBlank() }?.let { employee.passwordHash = passwordEncoder.encode(it)!! }

        if (CurrentEmployee.isAdmin()) {
            employee.roles = resolveRoles(normalized.roleIds)
        }

        return repository.save(employee)
    }

    @Transactional
    override fun delete(id: Long): Employee? {
        CurrentEmployee.requireSelfOrAdmin(id)
        val employee = getEmployee(id) ?: return null
        repository.delete(employee)
        repository.flush()
        return employee
    }

    private fun resolveRoles(roleIds: Set<Long>): MutableSet<Role> {
        val roles = roleRepository.findAllById(roleIds).toMutableSet()
        require(roles.size == roleIds.size) { "one or more role ids do not exist" }
        return roles
    }

    private fun normalize(request: EmployeeWriteDTO): EmployeeWriteDTO = request.copy(
        lastName = request.lastName.trim(),
        firstName = request.firstName.trim(),
        middleName = request.middleName?.trim()?.ifEmpty { null },
        username = request.username.trim(),
        email = request.email.trim().lowercase(),
    )
}
