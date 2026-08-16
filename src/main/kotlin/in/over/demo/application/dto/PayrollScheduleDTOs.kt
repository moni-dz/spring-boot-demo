package `in`.over.demo.application.dto

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Digits
import jakarta.validation.constraints.Future
import java.math.BigDecimal
import java.time.Instant

data class PayrollCreationScheduleRequestDTO(
    @field:Future
    val executeAt: Instant,
    val intervalStart: Instant,
    val intervalEnd: Instant,
    @field:DecimalMin(value = "0.0", inclusive = false)
    @field:Digits(integer = 15, fraction = 4)
    val hourlyRate: BigDecimal,
)

data class PayrollWageUpdateScheduleRequestDTO(
    @field:Future
    val executeAt: Instant,
)

data class StalePayrollDeletionScheduleRequestDTO(
    @field:Future
    val executeAt: Instant,
    val staleBefore: Instant,
)

data class PayrollScheduleDTO(
    val jobId: String,
    val scheduleId: String,
    val executeAt: Instant,
)
