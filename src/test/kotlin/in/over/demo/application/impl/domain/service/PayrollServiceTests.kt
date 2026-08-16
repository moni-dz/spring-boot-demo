package `in`.over.demo.application.impl.domain.service

import `in`.over.demo.BaseServiceTest
import `in`.over.demo.domain.model.PayrollRecord
import `in`.over.demo.domain.model.TimeRecord
import `in`.over.demo.domain.repository.PayrollRecordRepository
import `in`.over.demo.domain.repository.TimeRecordRepository
import `in`.over.demo.domain.service.PayrollService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.DataIntegrityViolationException
import java.math.BigDecimal
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@SpringBootTest
class PayrollServiceTests : BaseServiceTest {
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
    fun `update wage recalculates newly completed employee time`() {
        timeRecordRepository.save(
            TimeRecord(
                employeeId = 1,
                timeIn = Instant.parse("2026-01-04T00:00:00Z"),
                timeOut = Instant.parse("2026-01-04T01:00:00Z"),
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
    fun `old wage snapshots cannot be recalculated`() {
        val old = repository.saveAndFlush(
            PayrollRecord(
                employeeId = 1,
                intervalStart = Instant.parse("2027-01-01T00:00:00Z"),
                intervalEnd = Instant.parse("2027-01-31T00:00:00Z"),
                calculationVersion = 0,
                wageEarned = BigDecimal("123.4500"),
                createdAt = Instant.parse("2027-02-01T00:00:00Z"),
            ),
        )

        assertFailsWith<IllegalArgumentException> { service.updateWage(1, old.id) }
        assertEquals(0, repository.findById(old.id).orElseThrow().wageEarned.compareTo(BigDecimal("123.4500")))
    }

    @Test
    fun `soft delete marks only active stale records`() {
        assertEquals(1, service.softDeleteStale(1, Instant.parse("2025-06-01T00:00:00Z")))
        assertEquals(listOf(1L), service.list(1).map { it.id })
        assertNotNull(repository.findById(2).orElseThrow().deletedAt)
        assertTrue(repository.existsById(2))
        assertEquals(0, service.softDeleteStale(404, Instant.now()))
    }
}
