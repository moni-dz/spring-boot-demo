package fyi._4rsxyzt.demo.domain.repository

import fyi._4rsxyzt.demo.domain.model.TimeRecord
import org.springframework.data.jpa.repository.JpaRepository
import java.time.Instant

interface TimeRecordRepository : JpaRepository<TimeRecord, Long> {
    fun findFirstByEmployeeIdAndTimeOutIsNullOrderByIdDesc(employeeId: Long): TimeRecord?

    fun findAllByEmployeeIdAndTimeOutIsNotNullAndTimeInLessThanAndTimeOutGreaterThanOrderByTimeIn(
        employeeId: Long,
        intervalEnd: Instant,
        intervalStart: Instant,
    ): List<TimeRecord>
}
