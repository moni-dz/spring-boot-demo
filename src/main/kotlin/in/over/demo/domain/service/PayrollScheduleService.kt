package `in`.over.demo.domain.service

import `in`.over.demo.application.dto.PayrollCreationScheduleRequestDTO
import `in`.over.demo.application.dto.PayrollScheduleDTO
import `in`.over.demo.application.dto.PayrollWageUpdateScheduleRequestDTO
import `in`.over.demo.application.dto.StalePayrollDeletionScheduleRequestDTO

interface PayrollScheduleService {
    fun scheduleCreation(employeeId: Long, request: PayrollCreationScheduleRequestDTO): PayrollScheduleDTO?
    fun scheduleWageUpdate(
        employeeId: Long,
        payrollId: Long,
        request: PayrollWageUpdateScheduleRequestDTO,
    ): PayrollScheduleDTO?
    fun scheduleStaleDeletion(
        employeeId: Long,
        request: StalePayrollDeletionScheduleRequestDTO,
    ): PayrollScheduleDTO?
}
