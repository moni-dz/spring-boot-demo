package fyi._4rsxyzt.demo.application.controller

import fyi._4rsxyzt.demo.application.dto.PayrollCreationScheduleRequestDTO
import fyi._4rsxyzt.demo.application.dto.PayrollRecordDTO
import fyi._4rsxyzt.demo.application.dto.PayrollScheduleDTO
import fyi._4rsxyzt.demo.application.dto.PayrollWageUpdateScheduleRequestDTO
import fyi._4rsxyzt.demo.application.mapper.PayrollRecordMapper
import fyi._4rsxyzt.demo.application.nats.NatsEventPublisher
import fyi._4rsxyzt.demo.domain.service.PayrollScheduleService
import fyi._4rsxyzt.demo.domain.service.PayrollService
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/employee")
@Tag(name = "Payrolls")
@PreAuthorize("hasRole('ADMIN')")
class PayrollController(
    private val payrollService: PayrollService,
    private val scheduleService: PayrollScheduleService,
    private val mapper: PayrollRecordMapper,
    private val events: NatsEventPublisher,
) {
    @GetMapping("/{employeeId}/payrolls")
    fun list(@PathVariable employeeId: Long): List<PayrollRecordDTO> =
        mapper.toDtos(payrollService.list(employeeId))

    @PostMapping("/{employeeId}/payrolls/schedule-create")
    fun scheduleCreation(
        @PathVariable employeeId: Long,
        @Valid @RequestBody request: PayrollCreationScheduleRequestDTO,
    ): ResponseEntity<PayrollScheduleDTO> {
        val schedule = scheduleService.scheduleCreation(employeeId, request)
            ?: return ResponseEntity.notFound().build()
        events.publish("payroll", "created", schedule.scheduleId)
        return ResponseEntity.accepted().body(schedule)
    }

    @PostMapping("/{employeeId}/payrolls/{payrollId}/schedule-update")
    fun scheduleWageUpdate(
        @PathVariable employeeId: Long,
        @PathVariable payrollId: Long,
        @Valid @RequestBody request: PayrollWageUpdateScheduleRequestDTO,
    ): ResponseEntity<PayrollScheduleDTO> {
        val schedule = scheduleService.scheduleWageUpdate(employeeId, payrollId, request)
        events.publish("payroll", "updated", schedule.scheduleId)
        return ResponseEntity.accepted().body(schedule)
    }
}
