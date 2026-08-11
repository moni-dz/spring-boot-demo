package `in`.over.demo.application.controller

import `in`.over.demo.application.dto.TimeRecordDTO
import `in`.over.demo.application.mapper.TimeRecordMapper
import `in`.over.demo.domain.model.TimeRecord
import `in`.over.demo.domain.service.ITimeRecordService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * @property timeInEpoch time-in in Unix epoch seconds
 * @property timeOutEpoch time-out in Unix epoch seconds
 */
data class PartialTimeRecord(var timeInEpoch: Long?, var timeOutEpoch: Long?)

@RestController
@RequestMapping("/records")
class TimeRecordController(
    private val service: ITimeRecordService,
    private val mapper: TimeRecordMapper,
) {
    @RequestMapping("/status")
    fun health() = "Hello!"

    @GetMapping
    fun listRecords(): List<TimeRecordDTO> = mapper.toDtos(service.getRecords())

    @DeleteMapping
    fun deleteRecord(@RequestParam id: Long): ResponseEntity<TimeRecordDTO> {
        val deleted = service.delete(TimeRecord(
            id = TODO(),
            name = TODO(),
            timeInEpoch = TODO(),
            timeOutEpoch = TODO()
        ))

        return ResponseEntity.ok(mapper.toDto(deleted))
    }

    @PutMapping
    fun editRecord(
        @RequestParam id: Long,
        @RequestBody timeRecord: PartialTimeRecord,
    ): ResponseEntity<TimeRecordDTO> {
        val edited = service.edit(TimeRecord(
            id = TODO(),
            name = TODO(),
            timeInEpoch = TODO(),
            timeOutEpoch = TODO()
        ))

        return ResponseEntity.ok(mapper.toDto(edited))
    }


    @PostMapping("/time-in")
    fun timeIn(@RequestBody name: String): ResponseEntity<TimeRecordDTO> {
        val timedIn = service.timeIn(1)
        return ResponseEntity.ok(mapper.toDto(timedIn))
    }

    @PostMapping("/time-out")
    fun timeOut(@RequestBody name: String): ResponseEntity<TimeRecordDTO> {
        val timedOut = service.timeOut(1)
        return ResponseEntity.ok(mapper.toDto(timedOut))
    }
}
