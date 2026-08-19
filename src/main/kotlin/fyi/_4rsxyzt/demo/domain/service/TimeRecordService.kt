package fyi._4rsxyzt.demo.domain.service

import fyi._4rsxyzt.demo.application.dto.UpdateTimeRecordDTO
import fyi._4rsxyzt.demo.domain.model.TimeRecord

interface TimeRecordService {
    fun getRecords(): List<TimeRecord>
    fun insert(record: TimeRecord): TimeRecord
    fun update(id: Long, update: UpdateTimeRecordDTO): TimeRecord?
    fun delete(id: Long): TimeRecord?
    fun timeIn(employeeId: Long): TimeRecord?
    fun timeOut(employeeId: Long): TimeRecord?
}
