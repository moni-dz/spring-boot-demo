package `in`.over.demo.domain.repository

import `in`.over.demo.domain.model.PayrollRecord
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant

interface PayrollRecordRepository : JpaRepository<PayrollRecord, Long> {
    fun findAllByEmployeeIdAndDeletedAtIsNullOrderById(employeeId: Long): List<PayrollRecord>

    fun findByIdAndEmployeeIdAndDeletedAtIsNull(id: Long, employeeId: Long): PayrollRecord?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
        """
        SELECT record FROM PayrollRecord record
        WHERE record.id = :id
          AND record.employeeId = :employeeId
          AND record.deletedAt IS NULL
        """,
    )
    fun findActiveByIdForUpdate(
        @Param("id") id: Long,
        @Param("employeeId") employeeId: Long,
    ): PayrollRecord?

    fun existsByEmployeeIdAndDeletedAtIsNullAndIntervalStartLessThanAndIntervalEndGreaterThan(
        employeeId: Long,
        intervalEnd: Instant,
        intervalStart: Instant,
    ): Boolean

    @Modifying(clearAutomatically = true)
    @Query(
        """
        UPDATE PayrollRecord record
        SET record.deletedAt = :deletedAt
        WHERE record.employeeId = :employeeId
          AND record.deletedAt IS NULL
          AND record.intervalEnd < :staleBefore
        """,
    )
    fun softDeleteStale(
        @Param("employeeId") employeeId: Long,
        @Param("staleBefore") staleBefore: Instant,
        @Param("deletedAt") deletedAt: Instant,
    ): Int
}
