package `in`.over.demo.application.mapper

import `in`.over.demo.application.dto.TimeRecordDTO
import `in`.over.demo.domain.model.TimeRecord
import org.mapstruct.Mapper

@Mapper(componentModel = "spring")
interface TimeRecordMapper {
    fun toDto(record: TimeRecord): TimeRecordDTO
    fun toDtos(records: List<TimeRecord>): List<TimeRecordDTO>
}
