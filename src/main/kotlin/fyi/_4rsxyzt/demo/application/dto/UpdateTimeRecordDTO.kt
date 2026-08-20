package fyi._4rsxyzt.demo.application.dto

import jakarta.validation.constraints.AssertTrue
import java.time.Instant

data class UpdateTimeRecordDTO(
    val timeIn: Instant?,
    val timeOut: Instant?,
) {
    @get:AssertTrue(message = "timeOut must be after timeIn")
    val timeRangeValid: Boolean
        get() = timeIn == null || timeOut == null || timeOut.isAfter(timeIn)
}
