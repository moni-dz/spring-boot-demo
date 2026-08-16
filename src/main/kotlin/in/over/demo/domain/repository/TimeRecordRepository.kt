package `in`.over.demo.domain.repository

import `in`.over.demo.domain.model.TimeRecord
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface TimeRecordRepository : JpaRepository<TimeRecord, Long> {
    fun findFirstByEmployeeIdAndTimeOutEpochIsNullOrderByIdDesc(employeeId: Long): TimeRecord?

    @Query(
        """
        SELECT record FROM TimeRecord record
        WHERE record.employeeId = :employeeId
          AND record.timeInEpoch IS NOT NULL
          AND record.timeOutEpoch IS NOT NULL
          AND record.timeInEpoch < :intervalEndEpoch
          AND record.timeOutEpoch > :intervalStartEpoch
        ORDER BY record.timeInEpoch
        """,
    )
    fun findCompletedOverlapping(
        @Param("employeeId") employeeId: Long,
        @Param("intervalStartEpoch") intervalStartEpoch: Long,
        @Param("intervalEndEpoch") intervalEndEpoch: Long,
    ): List<TimeRecord>
}
