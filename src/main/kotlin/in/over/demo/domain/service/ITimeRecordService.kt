package `in`.over.demo.domain.service

import `in`.over.demo.domain.model.TimeRecord

interface ITimeRecordService {
    fun getRecords(): List<TimeRecord>
    fun insert(record: TimeRecord): TimeRecord
    fun edit(record: TimeRecord): TimeRecord
    fun delete(record: TimeRecord): TimeRecord
    fun timeIn(timeInEpoch: Long): TimeRecord
    fun timeOut(timeOutEpoch: Long): TimeRecord
}