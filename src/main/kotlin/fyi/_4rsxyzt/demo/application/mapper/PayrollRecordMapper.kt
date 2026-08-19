package fyi._4rsxyzt.demo.application.mapper

import fyi._4rsxyzt.demo.application.dto.PayrollRecordDTO
import fyi._4rsxyzt.demo.domain.model.PayrollRecord
import org.mapstruct.Mapper

@Mapper(componentModel = "spring")
interface PayrollRecordMapper {
    fun toDto(record: PayrollRecord): PayrollRecordDTO
    fun toDtos(records: List<PayrollRecord>): List<PayrollRecordDTO>
}
