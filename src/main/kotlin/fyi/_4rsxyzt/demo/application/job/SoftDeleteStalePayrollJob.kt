package fyi._4rsxyzt.demo.application.job

import fyi._4rsxyzt.demo.domain.repository.EmployeeRepository
import fyi._4rsxyzt.demo.domain.service.PayrollService
import org.quartz.DisallowConcurrentExecution
import org.quartz.JobExecutionContext
import org.springframework.scheduling.quartz.QuartzJobBean
import java.time.YearMonth
import java.time.ZoneId

@DisallowConcurrentExecution
class SoftDeleteStalePayrollJob(
    private val payrollService: PayrollService,
    private val employeeRepository: EmployeeRepository,
) : QuartzJobBean() {
    override fun executeInternal(context: JobExecutionContext) {
        val staleBefore = YearMonth.now(TIME_ZONE).atDay(1).atStartOfDay(TIME_ZONE).toInstant()

        employeeRepository.findAll().forEach { payrollService.softDeleteStale(it.id, staleBefore) }
    }

    private companion object {
        val TIME_ZONE: ZoneId = ZoneId.of("Asia/Manila")
    }
}
