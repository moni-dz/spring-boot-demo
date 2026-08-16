package `in`.over.demo.application.impl.domain.service

import `in`.over.demo.BaseServiceTest
import `in`.over.demo.application.dto.UpdateTimeRecordDTO
import `in`.over.demo.domain.model.TimeRecord
import `in`.over.demo.domain.repository.TimeRecordRepository
import `in`.over.demo.domain.service.TimeRecordService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.DataIntegrityViolationException
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@SpringBootTest
class TimeRecordServiceTests : BaseServiceTest {
    @Autowired
    private lateinit var service: TimeRecordService

    @Autowired
    private lateinit var repository: TimeRecordRepository

    @Test
    fun `getRecords returns records ordered by id`() {
        assertEquals(
            listOf(
                listOf(1L, 1L, Instant.parse("2026-01-02T00:00:00Z"), Instant.parse("2026-01-02T02:00:00Z")),
                listOf(2L, 1L, Instant.parse("2025-12-31T23:00:00Z"), Instant.parse("2026-01-01T01:00:00Z")),
                listOf(3L, 1L, Instant.parse("2026-01-03T00:00:00Z"), null),
                listOf(4L, 2L, Instant.parse("2026-01-02T00:00:00Z"), Instant.parse("2026-01-02T01:00:00Z")),
                listOf(5L, 1L, Instant.parse("2026-03-05T08:00:00Z"), Instant.parse("2026-03-05T10:30:00Z")),
                listOf(6L, 1L, Instant.parse("2026-03-05T09:30:00Z"), Instant.parse("2026-03-05T11:00:00Z")),
            ),
            service.getRecords().map { listOf(it.id, it.employeeId, it.timeIn, it.timeOut) },
        )
    }

    @Test
    fun `insert stores a new record`() {
        val inserted = service.insert(TimeRecord(employeeId = 2, timeIn = Instant.parse("2026-04-01T00:00:00Z")))

        assertTrue(inserted.id > 6)
        val stored = service.getRecords().last()
        assertEquals(inserted.id, stored.id)
        assertEquals(2, stored.employeeId)
        assertEquals(Instant.parse("2026-04-01T00:00:00Z"), stored.timeIn)
        assertNull(stored.timeOut)
    }

    @Test
    fun `database accepts zero-second entries and rejects reversed intervals`() {
        val timestamp = Instant.parse("2026-04-01T00:00:00Z")
        val sameSecond = repository.saveAndFlush(TimeRecord(employeeId = 2, timeIn = timestamp, timeOut = timestamp))

        assertTrue(sameSecond.id > 6)
        assertFailsWith<DataIntegrityViolationException> {
            repository.saveAndFlush(
                TimeRecord(employeeId = 2, timeIn = timestamp.plusSeconds(1), timeOut = timestamp),
            )
        }
    }

    @Test
    fun `update changes non-null fields and ignores unknown ids`() {
        val updated = service.update(
            4,
            UpdateTimeRecordDTO(timeIn = null, timeOut = Instant.parse("2026-01-02T02:00:00Z")),
        )

        assertNotNull(updated)
        assertEquals(
            listOf(4L, 2L, Instant.parse("2026-01-02T00:00:00Z"), Instant.parse("2026-01-02T02:00:00Z")),
            listOf(updated.id, updated.employeeId, updated.timeIn, updated.timeOut),
        )
        assertNull(service.update(404, UpdateTimeRecordDTO(Instant.EPOCH, Instant.EPOCH.plusSeconds(1))))
    }

    @Test
    fun `delete returns and removes a record and ignores unknown ids`() {
        val deleted = service.delete(4)

        assertNotNull(deleted)
        assertEquals(
            listOf(4L, 2L, Instant.parse("2026-01-02T00:00:00Z"), Instant.parse("2026-01-02T01:00:00Z")),
            listOf(deleted.id, deleted.employeeId, deleted.timeIn, deleted.timeOut),
        )
        assertEquals(listOf(1L, 2L, 3L, 5L, 6L), service.getRecords().map(TimeRecord::id))
        assertNull(service.delete(404))
    }

    @Test
    fun `timeIn stores current time`() {
        val before = Instant.now()
        val record = service.timeIn(2)
        val after = Instant.now()

        assertNotNull(record)
        assertEquals(2, record.employeeId)
        assertTrue(record.timeIn in before..after)
        assertNull(record.timeOut)
        assertNull(service.timeIn(404))
    }

    @Test
    fun `timeOut closes latest open record and ignores unknown names`() {
        val before = Instant.now()
        val newerOpen = service.insert(TimeRecord(employeeId = 1, timeIn = before))
        val closed = service.timeOut(1)
        val after = Instant.now()

        assertNotNull(closed)
        assertEquals(newerOpen.id, closed.id)
        assertTrue(closed.timeOut in before..after)
        assertNull(service.getRecords().single { it.id == 3L }.timeOut)
        assertNull(service.timeOut(404))
    }
}
