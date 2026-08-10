package `in`.over.demo

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
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
class TimeRecordApplicationTests {
    @Autowired
    private lateinit var appContext: WebApplicationContext

    @Autowired
    private lateinit var service: TimeRecordService

    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        service.records.clear()
        mockMvc = MockMvcBuilders.webAppContextSetup(appContext).build()
    }

    @Test
    fun `status reports that the API is available`() {
        mockMvc.perform(get("/records/status"))
            .andExpect(status().isOk)
            .andExpect(content().string("Hello!"))
    }

    @Test
    fun `listRecords returns all stored records`() {
        service.insert(TimeRecord(1, "Lythe", 100, null))
        service.insert(TimeRecord(2, "Marvin", 200, 250))

        mockMvc.perform(get("/records"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].name").value("Lythe"))
            .andExpect(jsonPath("$[0].timeInEpoch").value(100))
            .andExpect(jsonPath("$[0].timeOutEpoch").isEmpty)
            .andExpect(jsonPath("$[1].id").value(2))
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
            .andExpect(jsonPath("$.id").value(0))
            .andExpect(jsonPath("$.name").value("Lythe"))
            .andExpect(jsonPath("$.timeInEpoch").isNumber)
            .andExpect(jsonPath("$.timeOutEpoch").isEmpty)

        val createdRecord = service.records.single()
        val afterRequest = Clock.System.now().epochSeconds

        assertTrue(createdRecord.timeInEpoch in beforeRequest..afterRequest)
    }

    @Test
    fun `timeIn assigns an id after the current maximum`() {
        service.insert(TimeRecord(4, "Lythe", 100, 150))
        service.insert(TimeRecord(9, "Marvin", 200, null))

        mockMvc.perform(
            post("/records/time-in")
                .contentType(MediaType.TEXT_PLAIN)
                .content("Lim"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(10))
            .andExpect(jsonPath("$.name").value("Lim"))
    }

    @Test
    fun `timeOut closes the most recent open record for the name`() {
        service.insert(TimeRecord(1, "Lythe", 100, null))
        service.insert(TimeRecord(2, "Lythe", 200, null))
        service.insert(TimeRecord(3, "Lythe", 300, 350))

        val beforeRequest = Clock.System.now().epochSeconds

        mockMvc.perform(
            post("/records/time-out")
                .contentType(MediaType.TEXT_PLAIN)
                .content("Lythe"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(2))
            .andExpect(jsonPath("$.timeOutEpoch").isNumber)

        val afterRequest = Clock.System.now().epochSeconds

        assertNull(service.records[0].timeOutEpoch)
        assertTrue(service.records[1].timeOutEpoch in beforeRequest..afterRequest)
        assertEquals(350, service.records[2].timeOutEpoch)
    }

    @Test
    fun `timeOut returns not found when no open record matches`() {
        service.insert(TimeRecord(1, "Lythe", 100, 150))

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
        service.insert(TimeRecord(7, "Lythe", 100, null))

        mockMvc.perform(
            put("/records")
                .param("id", "7")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"timeInEpoch":null,"timeOutEpoch":250}"""),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(7))
            .andExpect(jsonPath("$.name").value("Lythe"))
            .andExpect(jsonPath("$.timeInEpoch").value(100))
            .andExpect(jsonPath("$.timeOutEpoch").value(250))

        assertEquals(TimeRecord(7, "Lythe", 100, 250), service.records.single())
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
        service.insert(TimeRecord(1, "Lythe", 100, null))
        service.insert(TimeRecord(2, "Marvin", 200, 250))

        mockMvc.perform(delete("/records").param("id", "1"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Lythe"))

        assertEquals(listOf(TimeRecord(2, "Marvin", 200, 250)), service.records)
    }
}
