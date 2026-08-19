package fyi._4rsxyzt.demo.domain.service

import fyi._4rsxyzt.demo.application.dto.EmployeeWriteDTO
import fyi._4rsxyzt.demo.domain.model.Employee

interface EmployeeService {
    fun getEmployees(): List<Employee>
    fun getEmployee(id: Long): Employee?
    fun create(request: EmployeeWriteDTO): Employee
    fun update(id: Long, request: EmployeeWriteDTO): Employee?
    fun delete(id: Long): Employee?
}
