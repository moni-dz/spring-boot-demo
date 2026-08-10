package `in`.over.demo

/**
 * @property timeInEpoch time-in in Unix epoch seconds
 * @property timeOutEpoch time-out in Unix epoch seconds
 */
data class PartialTimeRecord(var timeInEpoch: Long?, var timeOutEpoch: Long?)
