package `in`.over.demo.domain.repository

import `in`.over.demo.domain.model.Employee
import org.springframework.data.jpa.repository.JpaRepository

interface EmployeeRepository : JpaRepository<Employee, Long>
