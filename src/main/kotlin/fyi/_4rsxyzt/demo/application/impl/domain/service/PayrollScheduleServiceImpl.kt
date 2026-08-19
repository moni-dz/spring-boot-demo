package fyi._4rsxyzt.demo.application.impl.domain.service

import fyi._4rsxyzt.demo.application.dto.PayrollCreationScheduleRequestDTO
import fyi._4rsxyzt.demo.application.dto.PayrollScheduleDTO
import fyi._4rsxyzt.demo.application.dto.PayrollWageUpdateScheduleRequestDTO
import fyi._4rsxyzt.demo.application.job.CreatePayrollJob
import fyi._4rsxyzt.demo.application.job.SoftDeleteStalePayrollJob
import fyi._4rsxyzt.demo.application.job.UpdatePayrollWageJob
import fyi._4rsxyzt.demo.domain.repository.EmployeeRepository
import fyi._4rsxyzt.demo.domain.service.PayrollScheduleService
import jakarta.annotation.PostConstruct
import org.quartz.CronScheduleBuilder.cronSchedule
import org.quartz.JobBuilder.newJob
import org.quartz.JobDataMap
import org.quartz.JobDetail
import org.quartz.Scheduler
import org.quartz.TriggerBuilder.newTrigger
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.Date
import java.util.TimeZone
import java.util.UUID

@Service
class PayrollScheduleServiceImpl(
    private val scheduler: Scheduler,
    private val employeeRepository: EmployeeRepository,
    @Value($$"${payroll.stale-deletion.cron}") private val staleDeletionCron: String,
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
    ): PayrollScheduleDTO {
        val job = newJob(UpdatePayrollWageJob::class.java)
            .withIdentity("wage-$employeeId-$payrollId", GROUP)
            .usingJobData(UpdatePayrollWageJob.EMPLOYEE_ID, employeeId.toString())
            .usingJobData(UpdatePayrollWageJob.PAYROLL_ID, payrollId.toString())
            .storeDurably()
            .build()

        return schedule(job, request.executeAt, emptyMap())
    }

    @PostConstruct
    fun scheduleStaleDeletion() {
        val job = newJob(SoftDeleteStalePayrollJob::class.java)
            .withIdentity(STALE_JOB, GROUP)
            .storeDurably()
            .build()

        scheduler.addJob(job, true)

        val trigger = newTrigger()
            .withIdentity(STALE_TRIGGER, GROUP)
            .forJob(job.key)
            .withSchedule(cronSchedule(staleDeletionCron).inTimeZone(PAYROLL_TIME_ZONE))
            .build()

        if (scheduler.checkExists(trigger.key)) {
            scheduler.rescheduleJob(trigger.key, trigger)
        } else {
            scheduler.scheduleJob(trigger)
        }
    }

    private fun schedule(
        job: JobDetail,
        executeAt: Instant,
        data: Map<String, String>,
    ): PayrollScheduleDTO {
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
        const val STALE_JOB = "delete-stale"
        const val STALE_TRIGGER = "delete-stale-schedule"
        val PAYROLL_TIME_ZONE: TimeZone = TimeZone.getTimeZone("Asia/Manila")
    }
}
