package fyi._4rsxyzt.demo.application.impl.domain.service

import fyi._4rsxyzt.demo.application.dto.UpdateTimeRecordDTO
import fyi._4rsxyzt.demo.application.mapper.TimeRecordMapper
import fyi._4rsxyzt.demo.application.security.CurrentEmployee
import fyi._4rsxyzt.demo.domain.model.TimeRecord
import fyi._4rsxyzt.demo.domain.repository.EmployeeRepository
import fyi._4rsxyzt.demo.domain.repository.TimeRecordRepository
import fyi._4rsxyzt.demo.domain.service.TimeRecordService
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
        CurrentEmployee.requireSelfOrAdmin(record.employeeId)
        mapper.updateTime(update, record)
        return repository.save(record)
    }

    override fun delete(id: Long): TimeRecord? {
        val record = repository.findById(id).orElse(null) ?: return null
        CurrentEmployee.requireSelfOrAdmin(record.employeeId)
        repository.delete(record)
        return record
    }

    override fun timeIn(employeeId: Long): TimeRecord? {
        CurrentEmployee.requireSelfOrAdmin(employeeId)
        if (!employeeRepository.existsById(employeeId)) return null
        return repository.save(TimeRecord(employeeId = employeeId, timeIn = Instant.now()))
    }

    override fun timeOut(employeeId: Long): TimeRecord? {
        CurrentEmployee.requireSelfOrAdmin(employeeId)
        val record = repository.findFirstByEmployeeIdAndTimeOutIsNullOrderByIdDesc(employeeId) ?: return null
        record.timeOut = Instant.now()
        return repository.save(record)
    }
}
