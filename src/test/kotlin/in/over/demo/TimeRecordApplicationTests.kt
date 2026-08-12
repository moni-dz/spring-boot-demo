package `in`.over.demo

import `in`.over.demo.domain.model.TimeRecord
import `in`.over.demo.domain.repository.TimeRecordRepository
import `in`.over.demo.domain.service.TimeRecordService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock

@SpringBootTest
@Import(MySqlTestConfiguration::class)
class TimeRecordApplicationTests {
    @Autowired
    private lateinit var appContext: WebApplicationContext

    @Autowired
    private lateinit var service: TimeRecordService

    @Autowired
    private lateinit var repository: TimeRecordRepository

    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        repository.deleteAll()
        mockMvc = MockMvcBuilders.webAppContextSetup(appContext).build()
    }

    @Test
    fun `status reports that the API is available`() {
        mockMvc.perform(get("/records/status"))
            .andExpect(status().isOk)
            .andExpect(content().string("Hello!"))
    }

    @Test
    fun `OpenAPI documentation and Swagger UI are available`() {
        mockMvc.perform(get("/v3/api-docs"))
            .andExpect(status().isOk)
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.info.title").value("Time Record API"))
            .andExpect(jsonPath("$.paths['/records']").exists())
            .andExpect(
                jsonPath("$.paths['/records'].get.responses['200'].content['application/json'].schema.items['\$ref']")
                    .value("#/components/schemas/TimeRecordDTO"),
            )

        mockMvc.perform(get("/swagger-ui.html"))
            .andExpect(status().is3xxRedirection)
    }

    @Test
    fun `listRecords returns all stored records`() {
        val first = service.insert(TimeRecord(name = "Lythe", timeInEpoch = 100))
        val second = service.insert(TimeRecord(name = "Marvin", timeInEpoch = 200, timeOutEpoch = 250))

        mockMvc.perform(get("/records"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].id").value(first.id))
            .andExpect(jsonPath("$[0].name").value("Lythe"))
            .andExpect(jsonPath("$[0].timeInEpoch").value(100))
            .andExpect(jsonPath("$[0].timeOutEpoch").isEmpty)
            .andExpect(jsonPath("$[1].id").value(second.id))
            .andExpect(jsonPath("$[1].name").value("Marvin"))
            .andExpect(jsonPath("$[1].timeOutEpoch").value(250))
    }

    @Test
    fun `timeIn creates the first record`() {
        val beforeRequest = Clock.System.now().epochSeconds

        mockMvc.perform(
            post("/records/time-in")
                .contentType(MediaType.TEXT_PLAIN)
                .content("Lythe"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").isNumber)
            .andExpect(jsonPath("$.name").value("Lythe"))
            .andExpect(jsonPath("$.timeInEpoch").isNumber)
            .andExpect(jsonPath("$.timeOutEpoch").isEmpty)

        val createdRecord = service.getRecords().single()
        val afterRequest = Clock.System.now().epochSeconds

        assertTrue(createdRecord.timeInEpoch in beforeRequest..afterRequest)
    }

    @Test
    fun `timeIn assigns an id after the current maximum`() {
        service.insert(TimeRecord(name = "Lythe", timeInEpoch = 100, timeOutEpoch = 150))
        val currentMaximum = service.insert(TimeRecord(name = "Marvin", timeInEpoch = 200)).id

        mockMvc.perform(
            post("/records/time-in")
                .contentType(MediaType.TEXT_PLAIN)
                .content("Lim"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(currentMaximum + 1))
            .andExpect(jsonPath("$.name").value("Lim"))
    }

    @Test
    fun `timeOut closes the most recent open record for the name`() {
        service.insert(TimeRecord(name = "Lythe", timeInEpoch = 100))
        val latestOpen = service.insert(TimeRecord(name = "Lythe", timeInEpoch = 200))
        service.insert(TimeRecord(name = "Lythe", timeInEpoch = 300, timeOutEpoch = 350))

        val beforeRequest = Clock.System.now().epochSeconds

        mockMvc.perform(
            post("/records/time-out")
                .contentType(MediaType.TEXT_PLAIN)
                .content("Lythe"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(latestOpen.id))
            .andExpect(jsonPath("$.timeOutEpoch").isNumber)

        val afterRequest = Clock.System.now().epochSeconds

        val records = service.getRecords()
        assertNull(records[0].timeOutEpoch)
        assertTrue(records[1].timeOutEpoch in beforeRequest..afterRequest)
        assertEquals(350, records[2].timeOutEpoch)
    }

    @Test
    fun `timeOut returns not found when no open record matches`() {
        service.insert(TimeRecord(name = "Lythe", timeInEpoch = 100, timeOutEpoch = 150))

        mockMvc.perform(
            post("/records/time-out")
                .contentType(MediaType.TEXT_PLAIN)
                .content("Lythe"),
        )
            .andExpect(status().isNotFound)
            .andExpect(content().string(""))
    }

    @Test
    fun `editRecord updates only non-null fields`() {
        val record = service.insert(TimeRecord(name = "Lythe", timeInEpoch = 100))

        mockMvc.perform(
            put("/records")
                .param("id", record.id.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"timeInEpoch":null,"timeOutEpoch":250}"""),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(record.id))
            .andExpect(jsonPath("$.name").value("Lythe"))
            .andExpect(jsonPath("$.timeInEpoch").value(100))
            .andExpect(jsonPath("$.timeOutEpoch").value(250))

        assertEquals(TimeRecord(record.id, "Lythe", 100, 250), service.getRecords().single())
    }

    @Test
    fun `editRecord returns 404 for an unknown id`() {
        mockMvc.perform(
            put("/records")
                .param("id", "404")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"timeInEpoch":100,"timeOutEpoch":200}"""),
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun `deleteRecord returns and removes the matching record`() {
        val deleted = service.insert(TimeRecord(name = "Lythe", timeInEpoch = 100))
        val remaining = service.insert(TimeRecord(name = "Marvin", timeInEpoch = 200, timeOutEpoch = 250))

        mockMvc.perform(delete("/records").param("id", deleted.id.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(deleted.id))
            .andExpect(jsonPath("$.name").value("Lythe"))

        assertEquals(listOf(remaining), service.getRecords())
    }
}
