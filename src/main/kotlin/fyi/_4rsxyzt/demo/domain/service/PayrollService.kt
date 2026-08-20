package fyi._4rsxyzt.demo.domain.service

import fyi._4rsxyzt.demo.domain.model.PayrollRecord
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
    fun softDeleteStale(staleBefore: Instant): Int
}
