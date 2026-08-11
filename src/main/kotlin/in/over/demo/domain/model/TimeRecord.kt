package `in`.over.demo.domain.model

import jakarta.persistence.Entity
import jakarta.persistence.Id

/**
 * @property id sequential id
 * @property name the name of the user in the entry
 * @property timeInEpoch time-in in Unix epoch seconds
 * @property timeOutEpoch time-out in Unix epoch seconds
 */
@Entity
data class TimeRecord(
    @field:Id val id: Long = 0,
    val name: String = "",
    var timeInEpoch: Long? = null,
    var timeOutEpoch: Long? = null,
)
