package fyi._4rsxyzt.demo.domain.repository

import fyi._4rsxyzt.demo.domain.model.PayrollRecord
import org.springframework.data.jpa.repository.JpaRepository
import java.time.Instant

interface PayrollRecordRepository : JpaRepository<PayrollRecord, Long> {
    fun findAllByEmployeeIdAndDeletedAtIsNullOrderById(employeeId: Long): List<PayrollRecord>

    fun findByIdAndEmployeeIdAndDeletedAtIsNull(id: Long, employeeId: Long): PayrollRecord?

    fun findForUpdateByIdAndEmployeeIdAndDeletedAtIsNull(
        id: Long,
        employeeId: Long,
    ): PayrollRecord?

    fun findAllByEmployeeIdAndDeletedAtIsNullAndIntervalEndBefore(
        employeeId: Long,
        staleBefore: Instant,
    ): List<PayrollRecord>
}
