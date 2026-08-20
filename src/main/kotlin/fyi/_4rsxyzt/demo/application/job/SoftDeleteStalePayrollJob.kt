package fyi._4rsxyzt.demo.application.job

import fyi._4rsxyzt.demo.domain.service.PayrollService
import org.quartz.DisallowConcurrentExecution
import org.quartz.JobExecutionContext
import org.springframework.scheduling.quartz.QuartzJobBean
import java.time.YearMonth
import java.time.ZoneId

@DisallowConcurrentExecution
class SoftDeleteStalePayrollJob(
    private val payrollService: PayrollService,
) : QuartzJobBean() {
    override fun executeInternal(context: JobExecutionContext) {
        val staleBefore = YearMonth.now(TIME_ZONE).atDay(1).atStartOfDay(TIME_ZONE).toInstant()

        payrollService.softDeleteStale(staleBefore)
    }

    private companion object {
        val TIME_ZONE: ZoneId = ZoneId.of("Asia/Manila")
    }
}
