package fyi._4rsxyzt.demo.domain.service

import fyi._4rsxyzt.demo.application.dto.PayrollCreationScheduleRequestDTO
import fyi._4rsxyzt.demo.application.dto.PayrollScheduleDTO
import fyi._4rsxyzt.demo.application.dto.PayrollWageUpdateScheduleRequestDTO

interface PayrollScheduleService {
    fun scheduleCreation(employeeId: Long, request: PayrollCreationScheduleRequestDTO): PayrollScheduleDTO?
    fun scheduleWageUpdate(
        employeeId: Long,
        payrollId: Long,
        request: PayrollWageUpdateScheduleRequestDTO,
    ): PayrollScheduleDTO
}
