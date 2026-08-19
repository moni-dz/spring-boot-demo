package fyi._4rsxyzt.demo.application.dto

import java.math.BigDecimal
import java.time.Instant

data class PayrollCreationScheduleRequestDTO(
    val executeAt: Instant,
    val intervalStart: Instant,
    val intervalEnd: Instant,
    val hourlyRate: BigDecimal,
)

data class PayrollWageUpdateScheduleRequestDTO(
    val executeAt: Instant,
)

data class PayrollScheduleDTO(
    val jobId: String,
    val scheduleId: String,
    val executeAt: Instant,
)
