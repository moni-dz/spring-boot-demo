package `in`.over.demo.domain.service

import `in`.over.demo.application.dto.PayrollCreationScheduleRequestDTO
import `in`.over.demo.application.dto.PayrollScheduleDTO
import `in`.over.demo.application.dto.PayrollWageUpdateScheduleRequestDTO

interface PayrollScheduleService {
    fun scheduleCreation(employeeId: Long, request: PayrollCreationScheduleRequestDTO): PayrollScheduleDTO?
    fun scheduleWageUpdate(
        employeeId: Long,
        payrollId: Long,
        request: PayrollWageUpdateScheduleRequestDTO,
    ): PayrollScheduleDTO
}
