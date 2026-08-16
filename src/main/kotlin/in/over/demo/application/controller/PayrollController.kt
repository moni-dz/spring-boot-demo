package `in`.over.demo.application.controller

import `in`.over.demo.application.dto.PayrollCreationScheduleRequestDTO
import `in`.over.demo.application.dto.PayrollRecordDTO
import `in`.over.demo.application.dto.PayrollScheduleDTO
import `in`.over.demo.application.dto.PayrollWageUpdateScheduleRequestDTO
import `in`.over.demo.application.dto.StalePayrollDeletionScheduleRequestDTO
import `in`.over.demo.application.mapper.PayrollRecordMapper
import `in`.over.demo.domain.service.PayrollScheduleService
import `in`.over.demo.domain.service.PayrollService
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/employee")
@Tag(name = "Payrolls")
class PayrollController(
    private val payrollService: PayrollService,
    private val scheduleService: PayrollScheduleService,
    private val mapper: PayrollRecordMapper,
) {
    @GetMapping("/{employeeId}/payrolls")
    fun list(@PathVariable employeeId: Long): List<PayrollRecordDTO> =
        mapper.toDtos(payrollService.list(employeeId))

    @PostMapping("/{employeeId}/payrolls/creation-schedules")
    fun scheduleCreation(
        @PathVariable employeeId: Long,
        @Valid @RequestBody request: PayrollCreationScheduleRequestDTO,
    ): ResponseEntity<PayrollScheduleDTO> {
        val schedule = scheduleService.scheduleCreation(employeeId, request)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.accepted().body(schedule)
    }

    @PostMapping("/{employeeId}/payrolls/{payrollId}/wage-update-schedules")
    fun scheduleWageUpdate(
        @PathVariable employeeId: Long,
        @PathVariable payrollId: Long,
        @Valid @RequestBody request: PayrollWageUpdateScheduleRequestDTO,
    ): ResponseEntity<PayrollScheduleDTO> {
        val schedule = scheduleService.scheduleWageUpdate(employeeId, payrollId, request)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.accepted().body(schedule)
    }

    @PostMapping("/{employeeId}/payrolls/stale-deletion-schedules")
    fun scheduleStaleDeletion(
        @PathVariable employeeId: Long,
        @Valid @RequestBody request: StalePayrollDeletionScheduleRequestDTO,
    ): ResponseEntity<PayrollScheduleDTO> {
        val schedule = scheduleService.scheduleStaleDeletion(employeeId, request)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.accepted().body(schedule)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun invalidRequest(): ResponseEntity<Void> = ResponseEntity.badRequest().build()
}
