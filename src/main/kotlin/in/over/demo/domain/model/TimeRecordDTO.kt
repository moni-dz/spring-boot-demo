package `in`.over.demo.domain.model

data class TimeRecordDTO(
    val id: Long,
    val name: String,
    val timeInEpoch: Long?,
    val timeOutEpoch: Long?,
)
