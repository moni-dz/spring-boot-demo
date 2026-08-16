package `in`.over.demo.application.dto

import jakarta.validation.constraints.Positive

data class TimeRecordEntryDTO(
    @field:Positive
    val employeeId: Long,
)
