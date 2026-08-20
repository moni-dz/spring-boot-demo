package fyi._4rsxyzt.demo.application.impl.domain.service

import fyi._4rsxyzt.demo.domain.model.PayrollRecord
import fyi._4rsxyzt.demo.domain.repository.PayrollRecordRepository
import fyi._4rsxyzt.demo.domain.repository.TimeRecordRepository
import fyi._4rsxyzt.demo.domain.service.PayrollService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Duration
import java.time.Instant

@Service
class PayrollServiceImpl(
    private val repository: PayrollRecordRepository,
    private val timeRecordRepository: TimeRecordRepository,
) : PayrollService {
    override fun list(employeeId: Long): List<PayrollRecord> =
        repository.findAllByEmployeeIdAndDeletedAtIsNullOrderById(employeeId)

    override fun get(employeeId: Long, payrollId: Long): PayrollRecord? =
        repository.findByIdAndEmployeeIdAndDeletedAtIsNull(payrollId, employeeId)

    @Transactional
    override fun createForInterval(
        employeeId: Long,
        intervalStart: Instant,
        intervalEnd: Instant,
        hourlyRate: BigDecimal,
    ): PayrollRecord? = recalculate(
        PayrollRecord(
            employeeId = employeeId,
            intervalStart = intervalStart,
            intervalEnd = intervalEnd,
            hourlyRate = hourlyRate,
            createdAt = Instant.now(),
        ),
    )

    @Transactional
    override fun updateWage(employeeId: Long, payrollId: Long): PayrollRecord? {
        val record = repository.findForUpdateByIdAndEmployeeIdAndDeletedAtIsNull(payrollId, employeeId) ?: return null
        return recalculate(record)
    }

    @Transactional
    override fun softDeleteStale(staleBefore: Instant): Int =
        repository.softDeleteStale(staleBefore, Instant.now())

    private fun recalculate(record: PayrollRecord): PayrollRecord {
        require(record.calculationVersion == PayrollRecord.TIME_DERIVED_CALCULATION) {
            "old payroll records cannot be recalculated"
        }

        record.workedSeconds = workedSeconds(record)

        record.wageEarned = record.hourlyRate
            .multiply(BigDecimal.valueOf(record.workedSeconds))
            .divide(SECONDS_PER_HOUR, 4, RoundingMode.HALF_UP)

        return repository.save(record)
    }

    private fun workedSeconds(payroll: PayrollRecord): Long {
        val records = timeRecordRepository
            .findAllByEmployeeIdAndTimeOutIsNotNullAndTimeInLessThanAndTimeOutGreaterThanOrderByTimeIn(
                payroll.employeeId,
                payroll.intervalEnd,
                payroll.intervalStart,
            )

        var total = 0L
        var mergedStart: Instant? = null
        var mergedEnd = Instant.EPOCH

        for (record in records) {
            val start = maxOf(record.timeIn, payroll.intervalStart)
            // timeOut is guaranteed non-null by the TimeOutIsNotNull repo query above
            val end = minOf(record.timeOut!!, payroll.intervalEnd)

            if (mergedStart == null) {
                mergedStart = start
                mergedEnd = end
            } else if (start > mergedEnd) {
                total = Math.addExact(total, Duration.between(mergedStart, mergedEnd).seconds)
                mergedStart = start
                mergedEnd = end
            } else {
                mergedEnd = maxOf(mergedEnd, end)
            }
        }

        return if (mergedStart == null) 0 else Math.addExact(total, Duration.between(mergedStart, mergedEnd).seconds)
    }

    private companion object {
        val SECONDS_PER_HOUR: BigDecimal = BigDecimal.valueOf(3600)
    }
}
