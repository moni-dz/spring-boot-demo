package `in`.over.demo.domain.impl.domain.service

import `in`.over.demo.domain.model.TimeRecord
import `in`.over.demo.domain.repository.TimeRecordRepository
import `in`.over.demo.domain.service.ITimeRecordService
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import kotlin.time.Clock

@Service
class TimeRecordService(private val repository: TimeRecordRepository) : ITimeRecordService {
    override fun getRecords(): List<TimeRecord> = repository.findAll(Sort.by("id"))

    override fun insert(record: TimeRecord): TimeRecord = repository.save(record)

    override fun edit(id: Long, timeInEpoch: Long?, timeOutEpoch: Long?): TimeRecord? {
        val record = repository.findById(id).orElse(null) ?: return null
        timeInEpoch?.let { record.timeInEpoch = it }
        timeOutEpoch?.let { record.timeOutEpoch = it }
        return repository.save(record)
    }

    override fun delete(id: Long): TimeRecord? {
        val record = repository.findById(id).orElse(null) ?: return null
        repository.delete(record)
        return record
    }

    override fun timeIn(name: String): TimeRecord =
        repository.save(TimeRecord(name = name, timeInEpoch = Clock.System.now().epochSeconds))

    override fun timeOut(name: String): TimeRecord? {
        val record = repository.findFirstByNameAndTimeOutEpochIsNullOrderByIdDesc(name) ?: return null
        record.timeOutEpoch = Clock.System.now().epochSeconds
        return repository.save(record)
    }
}
