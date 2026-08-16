package `in`.over.demo.application.impl.domain.service

import `in`.over.demo.application.dto.PayrollCreationScheduleRequestDTO
import `in`.over.demo.application.dto.PayrollScheduleDTO
import `in`.over.demo.application.dto.PayrollWageUpdateScheduleRequestDTO
import `in`.over.demo.application.dto.StalePayrollDeletionScheduleRequestDTO
import `in`.over.demo.application.job.CreatePayrollJob
import `in`.over.demo.application.job.SoftDeleteStalePayrollJob
import `in`.over.demo.application.job.UpdatePayrollWageJob
import `in`.over.demo.domain.repository.EmployeeRepository
import `in`.over.demo.domain.service.PayrollScheduleService
import org.quartz.JobBuilder.newJob
import org.quartz.JobDataMap
import org.quartz.JobDetail
import org.quartz.Scheduler
import org.quartz.TriggerBuilder.newTrigger
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.Date
import java.util.UUID

@Service
class PayrollScheduleServiceImpl(
    private val scheduler: Scheduler,
    private val employeeRepository: EmployeeRepository,
) : PayrollScheduleService {
    override fun scheduleCreation(
        employeeId: Long,
        request: PayrollCreationScheduleRequestDTO,
    ): PayrollScheduleDTO? {
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

    private companion object {
        const val GROUP = "employee-payroll"
    }
}
