package `in`.over.demo.domain.service

import `in`.over.demo.domain.model.PayrollRecord
import java.math.BigDecimal
import java.time.Instant

interface PayrollService {
    fun list(employeeId: Long): List<PayrollRecord>
    fun get(employeeId: Long, payrollId: Long): PayrollRecord?
    fun createForInterval(
        employeeId: Long,
        intervalStart: Instant,
        intervalEnd: Instant,
        hourlyRate: BigDecimal,
    ): PayrollRecord?
    fun updateWage(employeeId: Long, payrollId: Long): PayrollRecord?
    fun softDeleteStale(employeeId: Long, staleBefore: Instant): Int?
}
