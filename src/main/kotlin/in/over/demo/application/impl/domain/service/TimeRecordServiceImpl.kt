package `in`.over.demo.application.impl.domain.service

import `in`.over.demo.application.dto.UpdateTimeRecordDTO
import `in`.over.demo.application.mapper.TimeRecordMapper
import `in`.over.demo.domain.model.TimeRecord
import `in`.over.demo.domain.repository.TimeRecordRepository
import `in`.over.demo.domain.service.TimeRecordService
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import kotlin.time.Clock

@Service
class TimeRecordServiceImpl(
    private val repository: TimeRecordRepository,
    private val mapper: TimeRecordMapper
) : TimeRecordService {
    override fun getRecords(): List<TimeRecord> = repository.findAll(Sort.by("id"))

    override fun insert(record: TimeRecord): TimeRecord = repository.save(record)

    override fun update(id: Long, update: UpdateTimeRecordDTO): TimeRecord? {
        val record = repository.findById(id).orElse(null) ?: return null
        mapper.updateTime(update, record)
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
