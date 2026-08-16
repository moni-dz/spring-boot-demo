package `in`.over.demo.application.impl.domain.service

import com.github.database.rider.core.api.configuration.DBUnit
import com.github.database.rider.core.api.dataset.DataSet
import com.github.database.rider.spring.api.DBRider
import `in`.over.demo.MySqlTestConfiguration
import `in`.over.demo.application.dto.EmployeeWriteDTO
import `in`.over.demo.domain.model.Employee
import `in`.over.demo.domain.service.EmployeeService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.dao.DataIntegrityViolationException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

@DBRider
@DBUnit(caseSensitiveTableNames = true, raiseExceptionOnCleanUp = true)
@DataSet(value = ["datasets/employees-payroll.yml"], cleanBefore = true)
@SpringBootTest
@Import(MySqlTestConfiguration::class)
class EmployeeServiceTests {
    @Autowired
    private lateinit var service: EmployeeService

    @Test
    fun `list and get return employees ordered by id`() {
        assertEquals(listOf(1L, 2L, 3L), service.getEmployees().map(Employee::id))
        assertEquals("Grace", service.getEmployee(2)?.firstName)
        assertNull(service.getEmployee(404))
    }

    @Test
    fun `create and update normalize granular names`() {
        val created = service.create(EmployeeWriteDTO(" Turing ", " Alan ", "  "))

        assertTrue(created.id > 3)
        assertEquals("Turing", created.lastName)
        assertEquals("Alan", created.firstName)
        assertNull(created.middleName)

        val updated = service.update(created.id, EmployeeWriteDTO(" Hamilton ", " Margaret ", " Heafield "))
        assertEquals(Employee(created.id, "Hamilton", "Margaret", "Heafield"), updated)
    }

    @Test
    fun `delete removes unreferenced employee and invalid names are rejected`() {
        assertEquals(3, service.delete(3)?.id)
        assertNull(service.getEmployee(3))
        assertNull(service.delete(404))
        assertFailsWith<DataIntegrityViolationException> { service.delete(1) }
        assertFailsWith<IllegalArgumentException> {
            service.create(EmployeeWriteDTO("Valid", "  "))
        }
    }
}
