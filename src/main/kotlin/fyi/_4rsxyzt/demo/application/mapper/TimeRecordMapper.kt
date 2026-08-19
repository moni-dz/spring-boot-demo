package fyi._4rsxyzt.demo.application.mapper

import fyi._4rsxyzt.demo.application.dto.TimeRecordDTO
import fyi._4rsxyzt.demo.application.dto.UpdateTimeRecordDTO
import fyi._4rsxyzt.demo.domain.model.TimeRecord
import org.mapstruct.BeanMapping
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingTarget
import org.mapstruct.NullValuePropertyMappingStrategy

@Mapper(componentModel = "spring")
interface TimeRecordMapper {
    fun toDto(record: TimeRecord): TimeRecordDTO
    fun toDtos(records: List<TimeRecord>): List<TimeRecordDTO>

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    fun updateTime(dto: UpdateTimeRecordDTO, @MappingTarget target: TimeRecord)
}
