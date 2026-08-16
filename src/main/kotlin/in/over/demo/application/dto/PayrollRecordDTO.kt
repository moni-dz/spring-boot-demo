package `in`.over.demo.application.dto

import java.math.BigDecimal
import java.time.Instant

data class PayrollRecordDTO(
    val id: Long,
    val employeeId: Long,
    val intervalStart: Instant,
    val intervalEnd: Instant,
    val hourlyRate: BigDecimal,
    val workedSeconds: Long,
    val wageEarned: BigDecimal,
    val createdAt: Instant,
)
