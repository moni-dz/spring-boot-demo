package `in`.over.demo.application.impl.domain.service

import `in`.over.demo.application.dto.UpdateTimeRecordDTO
import `in`.over.demo.application.mapper.TimeRecordMapper
import `in`.over.demo.domain.model.TimeRecord
import `in`.over.demo.domain.repository.EmployeeRepository
import `in`.over.demo.domain.repository.TimeRecordRepository
import `in`.over.demo.domain.service.TimeRecordService
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class TimeRecordServiceImpl(
    private val repository: TimeRecordRepository,
    private val employeeRepository: EmployeeRepository,
    private val mapper: TimeRecordMapper,
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

    override fun timeIn(employeeId: Long): TimeRecord? {
        if (!employeeRepository.existsById(employeeId)) return null
        return repository.save(TimeRecord(employeeId = employeeId, timeIn = Instant.now()))
    }

    override fun timeOut(employeeId: Long): TimeRecord? {
        val record = repository.findFirstByEmployeeIdAndTimeOutIsNullOrderByIdDesc(employeeId) ?: return null
        record.timeOut = Instant.now()
        return repository.save(record)
    }
}
