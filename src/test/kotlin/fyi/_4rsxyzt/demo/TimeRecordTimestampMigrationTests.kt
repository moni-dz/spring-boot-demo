package fyi._4rsxyzt.demo

import org.flywaydb.core.Flyway
import org.flywaydb.core.api.MigrationVersion
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.testcontainers.mysql.MySQLContainer
import java.sql.DriverManager
import java.sql.SQLException
import java.time.Instant
import java.util.Calendar
import java.util.TimeZone
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TimeRecordTimestampMigrationTests {
    private val mysql = MySQLContainer("mysql:9.7.1")

    @BeforeAll
    fun startDatabase() = mysql.start()

    @AfterAll
    fun stopDatabase() = mysql.stop()

    @Test
    fun `epoch values migrate without changing instants`() {
        Flyway.configure()
            .dataSource(mysql.jdbcUrl, mysql.username, mysql.password)
            .target(MigrationVersion.fromVersion("3"))
            .load()
            .migrate()

        DriverManager.getConnection(mysql.jdbcUrl, mysql.username, mysql.password).use { connection ->
            connection.createStatement().use { statement ->
                statement.executeUpdate("INSERT INTO employees (last_name, first_name) VALUES ('Lovelace', 'Ada')")
                statement.executeUpdate(
                    """
                    INSERT INTO time_records (employee_id, time_in_epoch, time_out_epoch)
                    VALUES (1, 1767312000, 1767319200), (1, 1767398400, NULL)
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
                statement.execute("SET SESSION time_zone = '+00:00'")
                val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                statement.executeQuery("SELECT id, time_in, time_out FROM time_records ORDER BY id").use { result ->
                    assertTrue(result.next())
                    assertEquals(Instant.parse("2026-01-02T00:00:00Z"), result.getTimestamp("time_in", utc).toInstant())
                    assertEquals(Instant.parse("2026-01-02T02:00:00Z"), result.getTimestamp("time_out", utc).toInstant())
                    assertTrue(result.next())
                    assertEquals(Instant.parse("2026-01-03T00:00:00Z"), result.getTimestamp("time_in", utc).toInstant())
                    assertNull(result.getTimestamp("time_out"))
                }

                statement.executeQuery(
                    """
                    SELECT COUNT(*)
                    FROM information_schema.columns
                    WHERE table_schema = DATABASE()
                      AND table_name = 'time_records'
                      AND column_name IN ('time_in_epoch', 'time_out_epoch')
                    """.trimIndent(),
                ).use { result ->
                    assertTrue(result.next())
                    assertEquals(0, result.getInt(1))
                }

                assertFailsWith<SQLException> {
                    statement.executeUpdate(
                        """
                        INSERT INTO time_records (employee_id, time_in, time_out)
                        VALUES (1, '2026-01-02 02:00:00', '2026-01-02 01:00:00')
                        """.trimIndent(),
                    )
                }
            }
        }
    }
}
