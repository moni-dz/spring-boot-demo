package `in`.over.demo

import `in`.over.demo.application.security.AuthenticatedEmployee
import com.github.database.rider.core.api.configuration.DBUnit
import com.github.database.rider.core.api.dataset.DataSet
import com.github.database.rider.spring.api.DBRider
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.springframework.context.annotation.Import
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder

@DBRider
@DBUnit(caseSensitiveTableNames = true, raiseExceptionOnCleanUp = true)
@DataSet(value = ["datasets/employees.yml"], cleanBefore = true)
@Import(MySqlTestConfiguration::class)
interface BaseServiceTest {
    @BeforeEach
    fun authenticateAsAdmin() {
        SecurityContextHolder.getContext().authentication = UsernamePasswordAuthenticationToken(
            AuthenticatedEmployee(0, "test-admin"),
            null,
            listOf(SimpleGrantedAuthority("ROLE_ADMIN")),
        )
    }

    @AfterEach
    fun clearAuthentication() {
        SecurityContextHolder.clearContext()
    }
}