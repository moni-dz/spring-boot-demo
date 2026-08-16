package `in`.over.demo.domain.repository

import `in`.over.demo.domain.model.Employee
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface EmployeeRepository : JpaRepository<Employee, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT employee FROM Employee employee WHERE employee.id = :id")
    fun findByIdForUpdate(@Param("id") id: Long): Employee?
}
