package fyi._4rsxyzt.demo.application.job

import fyi._4rsxyzt.demo.domain.service.PayrollService
import org.quartz.DisallowConcurrentExecution
import org.quartz.JobExecutionException
import org.quartz.JobExecutionContext
import org.springframework.scheduling.quartz.QuartzJobBean

@DisallowConcurrentExecution
class UpdatePayrollWageJob(
    private val payrollService: PayrollService,
) : QuartzJobBean() {
    override fun executeInternal(context: JobExecutionContext) {
        val data = context.mergedJobDataMap

        val updated = payrollService.updateWage(
            data.getString(EMPLOYEE_ID).toLong(),
            data.getString(PAYROLL_ID).toLong(),
        )

        if (updated == null) throw JobExecutionException("Payroll record no longer exists")
    }

    companion object {
        const val EMPLOYEE_ID = "employeeId"
        const val PAYROLL_ID = "payrollId"
    }
}
