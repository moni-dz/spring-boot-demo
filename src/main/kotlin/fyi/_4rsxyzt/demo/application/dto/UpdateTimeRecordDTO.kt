package fyi._4rsxyzt.demo.application.dto

import java.time.Instant

data class UpdateTimeRecordDTO(
    val timeIn: Instant?,
    val timeOut: Instant?,
)
