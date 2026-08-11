package `in`.over.demo.domain.repository

import `in`.over.demo.domain.model.TimeRecord
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface TimeRecordRepository : JpaRepository<TimeRecord, Long> {
    fun findFirstByNameAndTimeOutEpochIsNullOrderByIdDesc(name: String): TimeRecord?
}
