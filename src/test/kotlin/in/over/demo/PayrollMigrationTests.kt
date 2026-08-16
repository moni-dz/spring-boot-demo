package `in`.over.demo

import org.flywaydb.core.Flyway
import org.flywaydb.core.api.MigrationVersion
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.testcontainers.mysql.MySQLContainer
import java.math.BigDecimal
import java.sql.DriverManager
import java.sql.SQLException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PayrollMigrationTests {
    private val mysql = MySQLContainer("mysql:9.7.1")

    @BeforeAll
    fun startDatabase() = mysql.start()

    @AfterAll
    fun stopDatabase() = mysql.stop()

    @Test
    fun `legacy records migrate without losing wages`() {
        Flyway.configure()
            .dataSource(mysql.jdbcUrl, mysql.username, mysql.password)
            .target(MigrationVersion.fromVersion("3"))
            .load()
            .migrate()

        DriverManager.getConnection(mysql.jdbcUrl, mysql.username, mysql.password).use { connection ->
            connection.createStatement().use { statement ->
                statement.executeUpdate(
                    "INSERT INTO employees (last_name, first_name) VALUES ('Lovelace', 'Ada')",
                )
                statement.executeUpdate(
                    """
                    INSERT INTO payroll_records
                        (employee_id, interval_start, interval_end, wage_earned, created_at)
                    VALUES
                        (1, '2026-01-01 00:00:00', '2026-01-31 00:00:00', 123.4500, '2026-02-01 00:00:00')
                    """.trimIndent(),
                )
                statement.executeUpdate(
                    """
                    INSERT INTO time_records (name, time_in_epoch, time_out_epoch)
                    VALUES ('Ada Lovelace', 500, 500)
                    """.trimIndent(),
                )
            }
        }

        Flyway.configure()
            .dataSource(mysql.jdbcUrl, mysql.username, mysql.password)
            .load()
            .migrate()

        DriverManager.getConnection(mysql.jdbcUrl, mysql.username, mysql.password).use { connection ->
            connection.createStatement().use { statement ->
                statement.executeQuery(
                    "SELECT wage_earned, calculation_version FROM payroll_records WHERE id = 1",
                ).use { result ->
                    assertTrue(result.next())
                    assertEquals(0, result.getBigDecimal("wage_earned").compareTo(BigDecimal("123.4500")))
                    assertEquals(0, result.getInt("calculation_version"))
                }
                statement.executeQuery(
                    "SELECT employee_id, time_in_epoch, time_out_epoch FROM time_records WHERE id = 1",
                ).use { result ->
                    assertTrue(result.next())
                    assertTrue(result.getLong("employee_id") > 0)
                    assertEquals(500, result.getLong("time_in_epoch"))
                    assertEquals(500, result.getLong("time_out_epoch"))
                }
                assertFailsWith<SQLException> {
                    statement.executeUpdate(
                        """
                        INSERT INTO time_records (employee_id, time_out_epoch)
                        SELECT employee_id, 501 FROM time_records WHERE id = 1
                        """.trimIndent(),
                    )
                }
            }
        }
    }
}
