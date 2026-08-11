package `in`.over.demo.domain.model


/**
 * @property id sequential id
 * @property name the name of the user in the entry
 * @property timeInEpoch time-in in Unix epoch seconds
 * @property timeOutEpoch time-out in Unix epoch seconds
 */
data class TimeRecord(val id: Long, val name: String, var timeInEpoch: Long?, var timeOutEpoch: Long?)
