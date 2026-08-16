package `in`.over.demo.application.impl.domain.service

import com.github.database.rider.core.api.configuration.DBUnit
import com.github.database.rider.core.api.dataset.DataSet
import com.github.database.rider.spring.api.DBRider
import `in`.over.demo.MySqlTestConfiguration
import `in`.over.demo.application.dto.PayrollCreationScheduleRequestDTO
import `in`.over.demo.application.dto.PayrollScheduleDTO
import `in`.over.demo.application.dto.PayrollWageUpdateScheduleRequestDTO
import `in`.over.demo.application.dto.StalePayrollDeletionScheduleRequestDTO
import `in`.over.demo.application.job.CreatePayrollJob
import `in`.over.demo.application.job.SoftDeleteStalePayrollJob
import `in`.over.demo.application.job.UpdatePayrollWageJob
import `in`.over.demo.domain.model.TimeRecord
import `in`.over.demo.domain.service.PayrollScheduleService
import `in`.over.demo.domain.service.PayrollService
import `in`.over.demo.domain.service.TimeRecordService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import org.quartz.JobKey
import org.quartz.Scheduler
import org.quartz.TriggerKey
import org.quartz.impl.matchers.KeyMatcher
import org.quartz.listeners.JobListenerSupport
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@DBRider
@DBUnit(caseSensitiveTableNames = true, raiseExceptionOnCleanUp = true)
@DataSet(value = ["datasets/employees-payroll.yml"], cleanBefore = true)
@SpringBootTest
@Import(MySqlTestConfiguration::class)
class PayrollScheduleServiceTests {
    @Autowired
    private lateinit var service: PayrollScheduleService

    @Autowired
    private lateinit var scheduler: Scheduler

    @Autowired
    private lateinit var payrollService: PayrollService

    @Autowired
    private lateinit var timeRecordService: TimeRecordService

    @AfterEach
    fun clearSchedules() {
        if (!scheduler.isInStandbyMode) scheduler.standby()
        scheduler.clear()
    }

    @Test
    fun `creation schedule stores scalar job data and trigger`() {
        val executeAt = Instant.now().plusSeconds(3600)
        val schedule = service.scheduleCreation(
            1,
            PayrollCreationScheduleRequestDTO(
                executeAt,
                Instant.parse("2026-03-01T00:00:00Z"),
                Instant.parse("2026-03-31T00:00:00Z"),
                BigDecimal("50.0000"),
            ),
        )

        assertNotNull(schedule)
        val key = JobKey.jobKey(schedule.jobId, GROUP)
        val job = scheduler.getJobDetail(key)
        val trigger = scheduler.getTrigger(TriggerKey.triggerKey(schedule.scheduleId, GROUP))
        assertEquals(CreatePayrollJob::class.java, job.jobClass)
        assertEquals("1", job.jobDataMap.getString(CreatePayrollJob.EMPLOYEE_ID))
        assertEquals("50.0000", trigger.jobDataMap.getString(CreatePayrollJob.HOURLY_RATE))
        assertEquals(executeAt.toEpochMilli(), trigger.nextFireTime.time)
        val second = service.scheduleCreation(
            1,
            PayrollCreationScheduleRequestDTO(
                executeAt.plusSeconds(1),
                Instant.parse("2026-04-01T00:00:00Z"),
                Instant.parse("2026-04-30T00:00:00Z"),
                BigDecimal.ONE,
            ),
        )
        assertNotNull(second)
        assertEquals(schedule.jobId, second.jobId)
        assertNotEquals(schedule.scheduleId, second.scheduleId)
        assertNull(
            service.scheduleCreation(
                404,
                PayrollCreationScheduleRequestDTO(
                    executeAt,
                    Instant.EPOCH,
                    Instant.EPOCH.plusSeconds(1),
                    BigDecimal.ONE,
                ),
            ),
        )
    }

    @Test
    fun `creation schedule rejects sub-second payroll intervals`() {
        assertFailsWith<IllegalArgumentException> {
            service.scheduleCreation(
                1,
                PayrollCreationScheduleRequestDTO(
                    Instant.now().plusSeconds(3600),
                    Instant.parse("2026-03-01T00:00:00.001Z"),
                    Instant.parse("2026-03-31T00:00:00Z"),
                    BigDecimal.ONE,
                ),
            )
        }
    }

    @Test
    fun `wage update schedule belongs to employee payroll`() {
        val schedule = service.scheduleWageUpdate(
            1,
            1,
            PayrollWageUpdateScheduleRequestDTO(Instant.now().plusSeconds(3600)),
        )

        assertNotNull(schedule)
        val job = scheduler.getJobDetail(JobKey.jobKey(schedule.jobId, GROUP))
        val trigger = scheduler.getTrigger(TriggerKey.triggerKey(schedule.scheduleId, GROUP))
        assertEquals(UpdatePayrollWageJob::class.java, job.jobClass)
        assertEquals("1", job.jobDataMap.getString(UpdatePayrollWageJob.PAYROLL_ID))
        assertTrue(trigger.jobDataMap.isEmpty())
        assertNull(
            service.scheduleWageUpdate(
                2,
                1,
                PayrollWageUpdateScheduleRequestDTO(Instant.now().plusSeconds(3600)),
            ),
        )
    }

    @Test
    fun `stale deletion schedule stores employee cutoff`() {
        val staleBefore = Instant.parse("2025-06-01T00:00:00Z")
        val schedule = service.scheduleStaleDeletion(
            1,
            StalePayrollDeletionScheduleRequestDTO(Instant.now().plusSeconds(3600), staleBefore),
        )

        assertNotNull(schedule)
        val job = scheduler.getJobDetail(JobKey.jobKey(schedule.jobId, GROUP))
        val trigger = scheduler.getTrigger(TriggerKey.triggerKey(schedule.scheduleId, GROUP))
        assertEquals(SoftDeleteStalePayrollJob::class.java, job.jobClass)
        assertEquals(staleBefore.toString(), trigger.jobDataMap.getString(SoftDeleteStalePayrollJob.STALE_BEFORE))
    }

    @Test
    fun `Quartz executes create update and soft delete jobs`() {
        val executeAt = Instant.now().plusSeconds(3600)
        val intervalStart = Instant.parse("2026-03-01T00:00:00Z")
        val createdSchedule = service.scheduleCreation(
            1,
            PayrollCreationScheduleRequestDTO(
                executeAt,
                intervalStart,
                Instant.parse("2026-03-31T00:00:00Z"),
                BigDecimal("50.0000"),
            ),
        )
        assertNotNull(createdSchedule)
        execute(createdSchedule)

        val created = payrollService.list(1).single { it.intervalStart == intervalStart }
        assertEquals(10800, created.workedSeconds)
        assertEquals(0, created.wageEarned.compareTo(BigDecimal("150.0000")))
        timeRecordService.insert(
            TimeRecord(
                employeeId = 1,
                timeInEpoch = 1772708400,
                timeOutEpoch = 1772712000,
            ),
        )
        val wageSchedule = service.scheduleWageUpdate(
            1,
            created.id,
            PayrollWageUpdateScheduleRequestDTO(executeAt),
        )
        assertNotNull(wageSchedule)
        execute(wageSchedule)
        val updated = payrollService.get(1, created.id)!!
        assertEquals(14400, updated.workedSeconds)
        assertEquals(0, updated.wageEarned.compareTo(BigDecimal("200.0000")))

        val deletionSchedule = service.scheduleStaleDeletion(
            1,
            StalePayrollDeletionScheduleRequestDTO(executeAt, Instant.parse("2026-04-01T00:00:00Z")),
        )
        assertNotNull(deletionSchedule)
        execute(deletionSchedule)
        assertTrue(payrollService.list(1).isEmpty())
    }

    private fun execute(schedule: PayrollScheduleDTO) {
        val trigger = scheduler.getTrigger(TriggerKey.triggerKey(schedule.scheduleId, GROUP))
        val completed = CountDownLatch(1)
        val failure = AtomicReference<JobExecutionException?>(null)
        val listener = object : JobListenerSupport() {
            override fun getName() = "test-${UUID.randomUUID()}"

            override fun jobWasExecuted(context: JobExecutionContext, jobException: JobExecutionException?) {
                failure.set(jobException)
                completed.countDown()
            }
        }
        scheduler.listenerManager.addJobListener(listener, KeyMatcher.keyEquals(trigger.jobKey))
        scheduler.start()
        scheduler.triggerJob(trigger.jobKey, trigger.jobDataMap)

        assertTrue(completed.await(5, TimeUnit.SECONDS), "Quartz job did not complete")
        scheduler.listenerManager.removeJobListener(listener.name)
        assertNull(failure.get())
    }

    private companion object {
        const val GROUP = "employee-payroll"
    }
}
