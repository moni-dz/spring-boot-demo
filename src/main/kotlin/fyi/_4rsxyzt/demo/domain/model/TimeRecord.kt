package fyi._4rsxyzt.demo.domain.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

/**
 * @property id sequential id
 * @property employeeId employee owning the entry
 * @property timeIn time-in timestamp
 * @property timeOut time-out timestamp
 */
@Entity
@Table(name = "time_records")
class TimeRecord(
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    @field:Column(columnDefinition = "int")
    var id: Long = 0,
    @field:Column(name = "employee_id", nullable = false)
    var employeeId: Long = 0,
    @field:Column(name = "time_in", nullable = false)
    var timeIn: Instant = Instant.EPOCH,
    @field:Column(name = "time_out")
    var timeOut: Instant? = null,
)
