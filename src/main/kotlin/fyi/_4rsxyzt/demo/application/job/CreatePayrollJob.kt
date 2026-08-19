package fyi._4rsxyzt.demo.application.job

import fyi._4rsxyzt.demo.domain.service.PayrollService
import org.quartz.DisallowConcurrentExecution
import org.quartz.JobExecutionException
import org.quartz.JobExecutionContext
import org.springframework.scheduling.quartz.QuartzJobBean
import java.time.Instant

@DisallowConcurrentExecution
class CreatePayrollJob(
    private val payrollService: PayrollService,
) : QuartzJobBean() {
    override fun executeInternal(context: JobExecutionContext) {
        val data = context.mergedJobDataMap

        val created = payrollService.createForInterval(
            data.getString(EMPLOYEE_ID).toLong(),
            Instant.parse(data.getString(INTERVAL_START)),
            Instant.parse(data.getString(INTERVAL_END)),
            data.getString(HOURLY_RATE).toBigDecimal(),
        )

        if (created == null) throw JobExecutionException("Employee no longer exists")
    }

    companion object {
        const val EMPLOYEE_ID = "employeeId"
        const val INTERVAL_START = "intervalStart"
        const val INTERVAL_END = "intervalEnd"
        const val HOURLY_RATE = "hourlyRate"
    }
}
