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
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock

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
                TimeRecord(1, 1, 100, null),
                TimeRecord(2, 1, 200, null),
                TimeRecord(3, 1, 300, 350),
                TimeRecord(4, 2, 400, 450),
            ),
            service.getRecords(),
        )
    }

    @Test
    fun `insert stores a new record`() {
        val inserted = service.insert(TimeRecord(employeeId = 2, timeInEpoch = 500))

        assertTrue(inserted.id > 4)
        assertEquals(inserted, service.getRecords().last())
    }

    @Test
    fun `database accepts zero-second entries and rejects incomplete intervals`() {
        val sameSecond = repository.saveAndFlush(TimeRecord(employeeId = 2, timeInEpoch = 500, timeOutEpoch = 500))

        assertTrue(sameSecond.id > 4)
        assertFailsWith<DataIntegrityViolationException> {
            repository.saveAndFlush(TimeRecord(employeeId = 2, timeOutEpoch = 500))
        }
    }

    @Test
    fun `update changes non-null fields and ignores unknown ids`() {
        val updated = service.update(4, UpdateTimeRecordDTO(timeInEpoch = null, timeOutEpoch = 500))

        assertEquals(TimeRecord(4, 2, 400, 500), updated)
        assertNull(service.update(404, UpdateTimeRecordDTO(100, 200)))
    }

    @Test
    fun `delete returns and removes a record and ignores unknown ids`() {
        val deleted = service.delete(4)

        assertEquals(TimeRecord(4, 2, 400, 450), deleted)
        assertEquals(listOf(1L, 2L, 3L), service.getRecords().map(TimeRecord::id))
        assertNull(service.delete(404))
    }

    @Test
    fun `timeIn stores current time`() {
        val before = Clock.System.now().epochSeconds
        val record = service.timeIn(2)
        val after = Clock.System.now().epochSeconds

        assertNotNull(record)
        assertEquals(2, record.employeeId)
        assertTrue(record.timeInEpoch in before..after)
        assertNull(record.timeOutEpoch)
        assertNull(service.timeIn(404))
    }

    @Test
    fun `timeOut closes latest open record and ignores unknown names`() {
        val before = Clock.System.now().epochSeconds
        val closed = service.timeOut(1)
        val after = Clock.System.now().epochSeconds

        assertNotNull(closed)
        assertEquals(2, closed.id)
        assertTrue(closed.timeOutEpoch in before..after)
        assertNull(service.getRecords().first().timeOutEpoch)
        assertNull(service.timeOut(404))
    }
}
