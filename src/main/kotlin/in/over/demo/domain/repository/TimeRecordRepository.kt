package `in`.over.demo.domain.repository

import `in`.over.demo.domain.model.TimeRecord
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant

interface TimeRecordRepository : JpaRepository<TimeRecord, Long> {
    fun findFirstByEmployeeIdAndTimeOutIsNullOrderByIdDesc(employeeId: Long): TimeRecord?

    @Query(
        """
        SELECT record FROM TimeRecord record
        WHERE record.employeeId = :employeeId
          AND record.timeOut IS NOT NULL
          AND record.timeIn < :intervalEnd
          AND record.timeOut > :intervalStart
        ORDER BY record.timeIn
        """,
    )
    fun findCompletedOverlapping(
        @Param("employeeId") employeeId: Long,
        @Param("intervalStart") intervalStart: Instant,
        @Param("intervalEnd") intervalEnd: Instant,
    ): List<TimeRecord>
}
