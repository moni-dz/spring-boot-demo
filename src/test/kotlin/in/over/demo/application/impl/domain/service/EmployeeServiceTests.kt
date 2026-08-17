package `in`.over.demo.application.impl.domain.service

import `in`.over.demo.BaseServiceTest
import `in`.over.demo.application.dto.EmployeeWriteDTO
import `in`.over.demo.domain.model.Employee
import `in`.over.demo.domain.service.EmployeeService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.DataIntegrityViolationException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

@SpringBootTest
class EmployeeServiceTests : BaseServiceTest {
    @Autowired
    private lateinit var service: EmployeeService

    @Test
    fun `list and get return employees ordered by id`() {
        assertEquals(listOf(1L, 2L, 3L), service.getEmployees().map(Employee::id))
        assertEquals("Marvin", service.getEmployee(2)?.firstName)
        assertNull(service.getEmployee(404))
    }

    @Test
    fun `delete removes unreferenced employee and invalid names are rejected`() {
        assertEquals(3, service.delete(3)?.id)
        assertNull(service.getEmployee(3))
        assertNull(service.delete(404))
        assertFailsWith<DataIntegrityViolationException> { service.delete(1) }
        assertFailsWith<DataIntegrityViolationException> {
            service.create(
                EmployeeWriteDTO(
                    lastName = "Valid",
                    firstName = "  ",
                    username = "testuser",
                    email = "test@example.com",
                    password = "Password123!",
                ),
            )
        }
    }
}
