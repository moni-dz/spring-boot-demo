package fyi._4rsxyzt.demo.domain.repository

import fyi._4rsxyzt.demo.domain.model.PayrollRecord
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.time.Instant

interface PayrollRecordRepository : JpaRepository<PayrollRecord, Long> {
    fun findAllByEmployeeIdAndDeletedAtIsNullOrderById(employeeId: Long): List<PayrollRecord>

    fun findByIdAndEmployeeIdAndDeletedAtIsNull(id: Long, employeeId: Long): PayrollRecord?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PayrollRecord p where p.id = :id and p.employeeId = :employeeId and p.deletedAt is null")
    fun findForUpdateByIdAndEmployeeIdAndDeletedAtIsNull(id: Long, employeeId: Long): PayrollRecord?

    @Modifying(clearAutomatically = true)
    @Query(
        "update PayrollRecord p set p.deletedAt = :deletedAt " +
            "where p.deletedAt is null and p.intervalEnd < :staleBefore",
    )
    fun softDeleteStale(staleBefore: Instant, deletedAt: Instant): Int
}
