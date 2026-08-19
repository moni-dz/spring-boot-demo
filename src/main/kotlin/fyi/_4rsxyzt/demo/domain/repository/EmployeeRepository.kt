package fyi._4rsxyzt.demo.domain.repository

import fyi._4rsxyzt.demo.domain.model.Employee
import org.springframework.data.jpa.repository.JpaRepository

interface EmployeeRepository : JpaRepository<Employee, Long> {
    fun findByUsername(username: String): Employee?
}
