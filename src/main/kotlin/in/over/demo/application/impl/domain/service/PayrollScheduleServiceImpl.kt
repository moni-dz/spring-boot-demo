package `in`.over.demo.application.impl.domain.service

import `in`.over.demo.application.dto.PayrollCreationScheduleRequestDTO
import `in`.over.demo.application.dto.PayrollScheduleDTO
import `in`.over.demo.application.dto.PayrollWageUpdateScheduleRequestDTO
import `in`.over.demo.application.dto.StalePayrollDeletionScheduleRequestDTO
import `in`.over.demo.application.job.CreatePayrollJob
import `in`.over.demo.application.job.SoftDeleteStalePayrollJob
import `in`.over.demo.application.job.UpdatePayrollWageJob
import `in`.over.demo.domain.model.PayrollRecord
import `in`.over.demo.domain.repository.EmployeeRepository
import `in`.over.demo.domain.service.PayrollScheduleService
import `in`.over.demo.domain.service.PayrollService
import org.quartz.JobBuilder.newJob
import org.quartz.JobDataMap
import org.quartz.JobDetail
import org.quartz.Scheduler
import org.quartz.TriggerBuilder.newTrigger
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.Instant
import java.util.Date
import java.util.UUID

@Service
class PayrollScheduleServiceImpl(
    private val scheduler: Scheduler,
    private val employeeRepository: EmployeeRepository,
    private val payrollService: PayrollService,
) : PayrollScheduleService {
    override fun scheduleCreation(
        employeeId: Long,
        request: PayrollCreationScheduleRequestDTO,
    ): PayrollScheduleDTO? {
        validateFuture(request.executeAt)
        require(request.intervalStart < request.intervalEnd) { "intervalStart must be before intervalEnd" }
        require(request.intervalStart.nano == 0 && request.intervalEnd.nano == 0) {
            "payroll intervals use epoch-second precision"
        }
        validateHourlyRate(request.hourlyRate)
        if (!employeeRepository.existsById(employeeId)) return null

        val job = newJob(CreatePayrollJob::class.java)
            .withIdentity("create-$employeeId", GROUP)
            .usingJobData(CreatePayrollJob.EMPLOYEE_ID, employeeId.toString())
            .storeDurably()
            .build()
        return schedule(
            job,
            request.executeAt,
            mapOf(
                CreatePayrollJob.INTERVAL_START to request.intervalStart.toString(),
                CreatePayrollJob.INTERVAL_END to request.intervalEnd.toString(),
                CreatePayrollJob.HOURLY_RATE to request.hourlyRate.toPlainString(),
            ),
        )
    }

    override fun scheduleWageUpdate(
        employeeId: Long,
        payrollId: Long,
        request: PayrollWageUpdateScheduleRequestDTO,
    ): PayrollScheduleDTO? {
        validateFuture(request.executeAt)
        val payroll = payrollService.get(employeeId, payrollId) ?: return null
        require(payroll.calculationVersion == PayrollRecord.TIME_DERIVED_CALCULATION) {
            "legacy payroll records cannot be recalculated"
        }

        val job = newJob(UpdatePayrollWageJob::class.java)
            .withIdentity("wage-$employeeId-$payrollId", GROUP)
            .usingJobData(UpdatePayrollWageJob.EMPLOYEE_ID, employeeId.toString())
            .usingJobData(UpdatePayrollWageJob.PAYROLL_ID, payrollId.toString())
            .storeDurably()
            .build()
        return schedule(job, request.executeAt, emptyMap())
    }

    override fun scheduleStaleDeletion(
        employeeId: Long,
        request: StalePayrollDeletionScheduleRequestDTO,
    ): PayrollScheduleDTO? {
        validateFuture(request.executeAt)
        require(request.staleBefore <= request.executeAt) { "staleBefore must not be after executeAt" }
        if (!employeeRepository.existsById(employeeId)) return null

        val job = newJob(SoftDeleteStalePayrollJob::class.java)
            .withIdentity("delete-stale-$employeeId", GROUP)
            .usingJobData(SoftDeleteStalePayrollJob.EMPLOYEE_ID, employeeId.toString())
            .storeDurably()
            .build()
        return schedule(
            job,
            request.executeAt,
            mapOf(SoftDeleteStalePayrollJob.STALE_BEFORE to request.staleBefore.toString()),
        )
    }

    private fun schedule(job: JobDetail, executeAt: Instant, data: Map<String, String>): PayrollScheduleDTO {
        scheduler.addJob(job, true)
        val scheduleId = "${job.key.name}-${UUID.randomUUID()}"
        val trigger = newTrigger()
            .withIdentity(scheduleId, GROUP)
            .forJob(job.key)
            .usingJobData(JobDataMap(data))
            .startAt(Date.from(executeAt))
            .build()
        scheduler.scheduleJob(trigger)
        return PayrollScheduleDTO(job.key.name, scheduleId, executeAt)
    }

    private fun validateFuture(executeAt: Instant) {
        require(executeAt.isAfter(Instant.now())) { "executeAt must be in the future" }
    }

    private fun validateHourlyRate(hourlyRate: BigDecimal) {
        require(hourlyRate.signum() > 0) { "hourlyRate must be positive" }
        require(hourlyRate.scale() <= 4 && hourlyRate.precision() - hourlyRate.scale() <= 15) {
            "hourlyRate exceeds DECIMAL(19, 4)"
        }
    }

    private companion object {
        const val GROUP = "employee-payroll"
    }
}
