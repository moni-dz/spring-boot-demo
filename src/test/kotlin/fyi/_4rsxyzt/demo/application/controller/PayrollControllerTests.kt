package fyi._4rsxyzt.demo.application.controller

import fyi._4rsxyzt.demo.application.dto.PayrollWageUpdateScheduleRequestDTO
import fyi._4rsxyzt.demo.application.mapper.PayrollRecordMapper
import fyi._4rsxyzt.demo.application.nats.NatsEventPublisher
import fyi._4rsxyzt.demo.domain.service.PayrollScheduleService
import fyi._4rsxyzt.demo.domain.service.PayrollService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import org.springframework.http.HttpStatus
import java.time.Instant
import kotlin.test.assertEquals

class PayrollControllerTests {
    private val payrollService = mock(PayrollService::class.java)
    private val scheduleService = mock(PayrollScheduleService::class.java)
    private val mapper = mock(PayrollRecordMapper::class.java)
    private val events = mock(NatsEventPublisher::class.java)
    private val controller = PayrollController(payrollService, scheduleService, mapper, events)

    @Test
    fun `scheduleWageUpdate returns 404 without scheduling when payroll record is missing`() {
        `when`(payrollService.get(1, 404)).thenReturn(null)

        val response = controller.scheduleWageUpdate(
            1,
            404,
            PayrollWageUpdateScheduleRequestDTO(executeAt = Instant.now()),
        )

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        verifyNoInteractions(scheduleService, events)
    }
}
