package `in`.over.demo.application.impl.domain.service

import com.github.database.rider.core.api.configuration.DBUnit
import com.github.database.rider.core.api.dataset.DataSet
import com.github.database.rider.spring.api.DBRider
import `in`.over.demo.MySqlTestConfiguration
import `in`.over.demo.domain.model.PayrollRecord
import `in`.over.demo.domain.model.TimeRecord
import `in`.over.demo.domain.repository.PayrollRecordRepository
import `in`.over.demo.domain.repository.TimeRecordRepository
import `in`.over.demo.domain.service.PayrollService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.dao.DataIntegrityViolationException
import java.math.BigDecimal
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@DBRider
@DBUnit(caseSensitiveTableNames = true, raiseExceptionOnCleanUp = true)
@DataSet(value = ["datasets/employees-payroll.yml"], cleanBefore = true)
@SpringBootTest
@Import(MySqlTestConfiguration::class)
class PayrollServiceTests {
    @Autowired
    private lateinit var service: PayrollService

    @Autowired
    private lateinit var repository: PayrollRecordRepository

    @Autowired
    private lateinit var timeRecordRepository: TimeRecordRepository

    @Test
    fun `list and get expose only active records owned by employee`() {
        assertEquals(listOf(1L, 2L), service.list(1).map { it.id })
        assertEquals(1, service.get(1, 1)?.id)
        assertNull(service.get(2, 1))
        assertNull(service.get(1, 3))
    }

    @Test
    fun `create calculates wages from overlapping completed time records`() {
        val created = service.createForInterval(
            1,
            Instant.parse("2026-03-01T00:00:00Z"),
            Instant.parse("2026-03-31T00:00:00Z"),
            BigDecimal("50.0000"),
        )

        assertNotNull(created)
        assertTrue(created.id > 4)
        assertEquals(10800, created.workedSeconds)
        assertEquals(0, created.wageEarned.compareTo(BigDecimal("150.0000")))
        assertNull(service.createForInterval(404, Instant.EPOCH, Instant.EPOCH.plusSeconds(1), BigDecimal.ONE))
        assertFailsWith<IllegalArgumentException> {
            service.createForInterval(1, Instant.EPOCH, Instant.EPOCH, BigDecimal.ONE)
        }
        assertFailsWith<IllegalArgumentException> {
            service.createForInterval(
                1,
                Instant.parse("2026-01-15T00:00:00Z"),
                Instant.parse("2026-02-15T00:00:00Z"),
                BigDecimal.ONE,
            )
        }
        assertFailsWith<DataIntegrityViolationException> {
            repository.saveAndFlush(
                PayrollRecord(
                    employeeId = 1,
                    intervalStart = Instant.parse("2026-01-01T00:00:00Z"),
                    intervalEnd = Instant.parse("2026-01-31T00:00:00Z"),
                    hourlyRate = BigDecimal.ONE,
                    wageEarned = BigDecimal.ONE,
                    createdAt = Instant.now(),
                ),
            )
        }
    }

    @Test
    fun `update wage recalculates newly completed employee time`() {
        timeRecordRepository.save(
            TimeRecord(
                employeeId = 1,
                timeInEpoch = 1767484800,
                timeOutEpoch = 1767488400,
            ),
        )
        val updated = service.updateWage(1, 1)

        assertNotNull(updated)
        assertEquals(14400, updated.workedSeconds)
        assertEquals(0, updated.wageEarned.compareTo(BigDecimal("400.0000")))
        assertNull(service.updateWage(2, 1))
    }

    @Test
    fun `calculation supports the largest accepted hourly rate`() {
        val created = service.createForInterval(
            1,
            Instant.parse("2026-03-01T00:00:00Z"),
            Instant.parse("2026-03-31T00:00:00Z"),
            BigDecimal("999999999999999.9999"),
        )

        assertNotNull(created)
        assertEquals(0, created.wageEarned.compareTo(BigDecimal("2999999999999999.9997")))
    }

    @Test
    fun `legacy wage snapshots cannot be recalculated`() {
        val legacy = repository.saveAndFlush(
            PayrollRecord(
                employeeId = 1,
                intervalStart = Instant.parse("2027-01-01T00:00:00Z"),
                intervalEnd = Instant.parse("2027-01-31T00:00:00Z"),
                calculationVersion = 0,
                wageEarned = BigDecimal("123.4500"),
                createdAt = Instant.parse("2027-02-01T00:00:00Z"),
            ),
        )

        assertFailsWith<IllegalArgumentException> { service.updateWage(1, legacy.id) }
        assertEquals(0, repository.findById(legacy.id).orElseThrow().wageEarned.compareTo(BigDecimal("123.4500")))
    }

    @Test
    fun `soft delete marks only active stale records`() {
        assertEquals(1, service.softDeleteStale(1, Instant.parse("2025-06-01T00:00:00Z")))
        assertEquals(listOf(1L), service.list(1).map { it.id })
        assertNotNull(repository.findById(2).orElseThrow().deletedAt)
        assertTrue(repository.existsById(2))
        assertNull(service.softDeleteStale(404, Instant.now()))
    }
}
