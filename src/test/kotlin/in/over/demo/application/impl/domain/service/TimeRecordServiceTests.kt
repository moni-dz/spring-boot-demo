package `in`.over.demo.application.impl.domain.service

import com.github.database.rider.core.api.configuration.DBUnit
import com.github.database.rider.core.api.dataset.DataSet
import com.github.database.rider.spring.api.DBRider
import `in`.over.demo.MySqlTestConfiguration
import `in`.over.demo.application.dto.UpdateTimeRecordDTO
import `in`.over.demo.domain.model.TimeRecord
import `in`.over.demo.domain.service.TimeRecordService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock

@DBRider
@DBUnit(caseSensitiveTableNames = true)
@DataSet(value = ["datasets/time-records.yml"], cleanBefore = true)
@SpringBootTest
@Import(MySqlTestConfiguration::class)
class TimeRecordServiceTests {
    @Autowired
    private lateinit var service: TimeRecordService

    @Test
    fun `getRecords returns records ordered by id`() {
        assertEquals(
            listOf(
                TimeRecord(1, "Lythe", 100, null),
                TimeRecord(2, "Lythe", 200, null),
                TimeRecord(3, "Lythe", 300, 350),
                TimeRecord(4, "Marvin", 400, 450),
            ),
            service.getRecords(),
        )
    }

    @Test
    fun `insert stores a new record`() {
        val inserted = service.insert(TimeRecord(name = "Lim", timeInEpoch = 500))

        assertTrue(inserted.id > 4)
        assertEquals(inserted, service.getRecords().last())
    }

    @Test
    fun `update changes non-null fields and ignores unknown ids`() {
        val updated = service.update(4, UpdateTimeRecordDTO(timeInEpoch = null, timeOutEpoch = 500))

        assertEquals(TimeRecord(4, "Marvin", 400, 500), updated)
        assertNull(service.update(404, UpdateTimeRecordDTO(100, 200)))
    }

    @Test
    fun `delete returns and removes a record and ignores unknown ids`() {
        val deleted = service.delete(4)

        assertEquals(TimeRecord(4, "Marvin", 400, 450), deleted)
        assertEquals(listOf(1L, 2L, 3L), service.getRecords().map(TimeRecord::id))
        assertNull(service.delete(404))
    }

    @Test
    fun `timeIn stores current time`() {
        val before = Clock.System.now().epochSeconds
        val record = service.timeIn("Lim")
        val after = Clock.System.now().epochSeconds

        assertEquals("Lim", record.name)
        assertTrue(record.timeInEpoch in before..after)
        assertNull(record.timeOutEpoch)
    }

    @Test
    fun `timeOut closes latest open record and ignores unknown names`() {
        val before = Clock.System.now().epochSeconds
        val closed = service.timeOut("Lythe")
        val after = Clock.System.now().epochSeconds

        assertNotNull(closed)
        assertEquals(2, closed.id)
        assertTrue(closed.timeOutEpoch in before..after)
        assertNull(service.getRecords().first().timeOutEpoch)
        assertNull(service.timeOut("Unknown"))
    }
}
