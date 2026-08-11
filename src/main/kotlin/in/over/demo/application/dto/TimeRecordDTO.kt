package `in`.over.demo.application.dto

data class TimeRecordDTO(
    val id: Long,
    val name: String,
    val timeInEpoch: Long?,
    val timeOutEpoch: Long?,
)
