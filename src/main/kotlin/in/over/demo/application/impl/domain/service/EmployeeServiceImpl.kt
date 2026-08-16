package `in`.over.demo.application.impl.domain.service

import `in`.over.demo.application.dto.EmployeeWriteDTO
import `in`.over.demo.application.mapper.EmployeeMapper
import `in`.over.demo.domain.model.Employee
import `in`.over.demo.domain.repository.EmployeeRepository
import `in`.over.demo.domain.service.EmployeeService
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class EmployeeServiceImpl(
    private val repository: EmployeeRepository,
    private val mapper: EmployeeMapper,
) : EmployeeService {
    override fun getEmployees(): List<Employee> = repository.findAll(Sort.by("id"))

    override fun getEmployee(id: Long): Employee? = repository.findById(id).orElse(null)

    override fun create(request: EmployeeWriteDTO): Employee = repository.save(mapper.toModel(normalize(request)))

    override fun update(id: Long, request: EmployeeWriteDTO): Employee? {
        val employee = getEmployee(id) ?: return null
        mapper.update(normalize(request), employee)
        return repository.save(employee)
    }

    @Transactional
    override fun delete(id: Long): Employee? {
        val employee = getEmployee(id) ?: return null
        repository.delete(employee)
        repository.flush()
        return employee
    }

    private fun normalize(request: EmployeeWriteDTO): EmployeeWriteDTO {
        val normalized = request.copy(
            lastName = request.lastName.trim(),
            firstName = request.firstName.trim(),
            middleName = request.middleName?.trim()?.ifEmpty { null },
        )
        require(normalized.lastName.isNotEmpty() && normalized.lastName.length <= 100) { "invalid lastName" }
        require(normalized.firstName.isNotEmpty() && normalized.firstName.length <= 100) { "invalid firstName" }
        require(normalized.middleName == null || normalized.middleName.length <= 100) { "invalid middleName" }
        return normalized
    }
}
