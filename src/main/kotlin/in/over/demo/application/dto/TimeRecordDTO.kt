package `in`.over.demo.application.dto

data class TimeRecordDTO(
    val id: Long,
    val employeeId: Long,
    val timeInEpoch: Long?,
    val timeOutEpoch: Long?,
)
