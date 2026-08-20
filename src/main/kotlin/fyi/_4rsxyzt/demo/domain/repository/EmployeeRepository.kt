package fyi._4rsxyzt.demo.domain.repository

import fyi._4rsxyzt.demo.domain.model.Employee
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface EmployeeRepository : JpaRepository<Employee, Long> {
    @EntityGraph(attributePaths = ["roles"])
    fun findByUsername(username: String): Employee?

    @EntityGraph(attributePaths = ["roles"])
    override fun findAll(sort: Sort): List<Employee>

    @EntityGraph(attributePaths = ["roles"])
    override fun findById(id: Long): Optional<Employee>
}
