package `in`.over.demo.application.impl.domain.service

import `in`.over.demo.domain.model.PayrollRecord
import `in`.over.demo.domain.repository.PayrollRecordRepository
import `in`.over.demo.domain.repository.TimeRecordRepository
import `in`.over.demo.domain.service.PayrollService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
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
        val record = repository.findActiveByIdForUpdate(payrollId, employeeId) ?: return null
        return recalculate(record)
    }

    @Transactional
    override fun softDeleteStale(employeeId: Long, staleBefore: Instant): Int? =
        repository.softDeleteStale(employeeId, staleBefore, Instant.now())

    private fun recalculate(record: PayrollRecord): PayrollRecord {
        require(record.calculationVersion == PayrollRecord.TIME_DERIVED_CALCULATION) {
            "legacy payroll records cannot be recalculated"
        }
        record.workedSeconds = workedSeconds(record)

        record.wageEarned = record.hourlyRate
            .multiply(BigDecimal.valueOf(record.workedSeconds))
            .divide(SECONDS_PER_HOUR, 4, RoundingMode.HALF_UP)

        return repository.save(record)
    }

    private fun workedSeconds(payroll: PayrollRecord): Long {
        val intervalStart = payroll.intervalStart.epochSecond
        val intervalEnd = payroll.intervalEnd.epochSecond

        val records = timeRecordRepository.findCompletedOverlapping(
            payroll.employeeId,
            intervalStart,
            intervalEnd,
        )

        var total = 0L
        var mergedStart: Long? = null
        var mergedEnd = 0L

        for (record in records) {
            val start = maxOf(record.timeInEpoch!!, intervalStart)
            val end = minOf(record.timeOutEpoch!!, intervalEnd)

            if (mergedStart == null) {
                mergedStart = start
                mergedEnd = end
            } else if (start > mergedEnd) {
                total = Math.addExact(total, mergedEnd - mergedStart)
                mergedStart = start
                mergedEnd = end
            } else {
                mergedEnd = maxOf(mergedEnd, end)
            }
        }

        return if (mergedStart == null) 0 else Math.addExact(total, mergedEnd - mergedStart)
    }

    private companion object {
        val SECONDS_PER_HOUR: BigDecimal = BigDecimal.valueOf(3600)
    }
}
