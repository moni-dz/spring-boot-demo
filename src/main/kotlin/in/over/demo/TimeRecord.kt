package `in`.over.demo

import kotlin.time.Instant

/**
 * @property name the name of the user in the entry
 * @property timeInEpoch time-in in Unix epoch seconds
 * @property timeOutEpoch time-out in Unix epoch seconds
 */
data class TimeRecord(val id: Long, val name: String, var timeInEpoch: Long?, var timeOutEpoch: Long?)
