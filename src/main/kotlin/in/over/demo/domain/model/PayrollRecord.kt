package `in`.over.demo.domain.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(name = "payroll_records")
class PayrollRecord(
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,
    @field:Column(name = "employee_id", nullable = false)
    var employeeId: Long = 0,
    @field:Column(name = "interval_start", nullable = false)
    var intervalStart: Instant = Instant.EPOCH,
    @field:Column(name = "interval_end", nullable = false)
    var intervalEnd: Instant = Instant.EPOCH,
    @field:Column(name = "hourly_rate", nullable = false, precision = 19, scale = 4)
    var hourlyRate: BigDecimal = BigDecimal.ZERO,
    @field:Column(name = "worked_seconds", nullable = false)
    var workedSeconds: Long = 0,
    @field:Column(name = "calculation_version", nullable = false)
    var calculationVersion: Int = TIME_DERIVED_CALCULATION,
    @field:Column(name = "wage_earned", nullable = false, precision = 38, scale = 4)
    var wageEarned: BigDecimal = BigDecimal.ZERO,
    @field:Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.EPOCH,
    @field:Column(name = "deleted_at")
    var deletedAt: Instant? = null,
) {
    companion object {
        const val TIME_DERIVED_CALCULATION = 1
    }
}
