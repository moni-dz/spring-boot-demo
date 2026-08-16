package `in`.over.demo.application.mapper

import `in`.over.demo.application.dto.PayrollRecordDTO
import `in`.over.demo.domain.model.PayrollRecord
import org.mapstruct.Mapper

@Mapper(componentModel = "spring")
interface PayrollRecordMapper {
    fun toDto(record: PayrollRecord): PayrollRecordDTO
    fun toDtos(records: List<PayrollRecord>): List<PayrollRecordDTO>
}
