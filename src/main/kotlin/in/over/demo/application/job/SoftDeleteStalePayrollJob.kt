package `in`.over.demo.application.job

import `in`.over.demo.domain.service.PayrollService
import org.quartz.DisallowConcurrentExecution
import org.quartz.JobExecutionException
import org.quartz.JobExecutionContext
import org.springframework.scheduling.quartz.QuartzJobBean
import java.time.Instant

@DisallowConcurrentExecution
class SoftDeleteStalePayrollJob(
    private val payrollService: PayrollService,
) : QuartzJobBean() {
    override fun executeInternal(context: JobExecutionContext) {
        val data = context.mergedJobDataMap

        val deleted = payrollService.softDeleteStale(
            data.getString(EMPLOYEE_ID).toLong(),
            Instant.parse(data.getString(STALE_BEFORE)),
        )

        if (deleted == null) throw JobExecutionException("Employee no longer exists")
    }

    companion object {
        const val EMPLOYEE_ID = "employeeId"
        const val STALE_BEFORE = "staleBefore"
    }
}
