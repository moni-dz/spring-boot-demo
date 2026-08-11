package `in`.over.demo.domain.service

import `in`.over.demo.domain.model.TimeRecord
import org.springframework.stereotype.Service

@Service
class TimeRecordService {
    val records = mutableListOf<TimeRecord>()

    fun insert(record: TimeRecord): TimeRecord {
        records.add(record)
        return record
    }

}