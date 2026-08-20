package fyi._4rsxyzt.demo.application.impl.domain.service

import fyi._4rsxyzt.demo.application.dto.EmployeeWriteDTO
import fyi._4rsxyzt.demo.application.mapper.EmployeeMapper
import fyi._4rsxyzt.demo.application.security.CurrentEmployee
import fyi._4rsxyzt.demo.domain.model.Employee
import fyi._4rsxyzt.demo.domain.model.Role
import fyi._4rsxyzt.demo.domain.repository.EmployeeRepository
import fyi._4rsxyzt.demo.domain.repository.RoleRepository
import fyi._4rsxyzt.demo.domain.service.EmployeeService
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

    @Transactional
    override fun create(request: EmployeeWriteDTO): Employee {
        val normalized = normalize(request)
        val password = normalized.password?.takeIf { it.isNotBlank() } ?: throw IllegalArgumentException("password is required")

        val employee = mapper.toModel(normalized)
        employee.passwordHash = passwordEncoder.encode(password)!!
        employee.roles = resolveRoles(normalized.roleIds)

        return repository.save(employee)
    }

    @Transactional
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
