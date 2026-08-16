package `in`.over.demo.domain.service

import `in`.over.demo.application.dto.EmployeeWriteDTO
import `in`.over.demo.domain.model.Employee

interface EmployeeService {
    fun getEmployees(): List<Employee>
    fun getEmployee(id: Long): Employee?
    fun create(request: EmployeeWriteDTO): Employee
    fun update(id: Long, request: EmployeeWriteDTO): Employee?
    fun delete(id: Long): Employee?
}
