package `in`.over.demo.domain.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

/**
 * @property id sequential id
 * @property name the name of the user in the entry
 * @property timeInEpoch time-in in Unix epoch seconds
 * @property timeOutEpoch time-out in Unix epoch seconds
 */
@Entity
@Table(name = "time_records")
data class TimeRecord(
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    @field:Column(columnDefinition = "int")
    var id: Long = 0,
    @field:Column(length = 1000)
    val name: String = "",
    @field:Column(name = "time_in_epoch", columnDefinition = "int unsigned")
    var timeInEpoch: Long? = null,
    @field:Column(name = "time_out_epoch", columnDefinition = "int unsigned")
    var timeOutEpoch: Long? = null,
)
