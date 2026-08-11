package `in`.over.demo.domain.service

import `in`.over.demo.domain.model.TimeRecord

interface ITimeRecordService {
    fun getRecords(): List<TimeRecord>
    fun insert(record: TimeRecord): TimeRecord
    fun edit(id: Long, timeInEpoch: Long?, timeOutEpoch: Long?): TimeRecord?
    fun delete(id: Long): TimeRecord?
    fun timeIn(name: String): TimeRecord
    fun timeOut(name: String): TimeRecord?
}
