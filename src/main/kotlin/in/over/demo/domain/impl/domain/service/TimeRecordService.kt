package `in`.over.demo.domain.impl.domain.service

import `in`.over.demo.domain.model.TimeRecord
import `in`.over.demo.domain.service.ITimeRecordService
import org.springframework.stereotype.Service

@Service
class TimeRecordService: ITimeRecordService {
    override fun getRecords(): List<TimeRecord> {
        TODO("Not yet implemented")
    }

    override fun insert(record: TimeRecord): TimeRecord {
        TODO("Not yet implemented")
    }

    override fun edit(record: TimeRecord): TimeRecord {
        TODO("Not yet implemented")
    }

    override fun delete(record: TimeRecord): TimeRecord {
        TODO("Not yet implemented")
    }

    override fun timeIn(timeInEpoch: Long): TimeRecord {
        TODO("Not yet implemented")
    }

    override fun timeOut(timeOutEpoch: Long): TimeRecord {
        TODO("Not yet implemented")
    }

}