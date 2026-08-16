package `in`.over.demo.application.dto

import java.time.Instant

data class TimeRecordDTO(
    val id: Long,
    val employeeId: Long,
    val timeIn: Instant,
    val timeOut: Instant?,
)
