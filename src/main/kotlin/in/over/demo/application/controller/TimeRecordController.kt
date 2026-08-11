package `in`.over.demo.application.controller

import `in`.over.demo.application.dto.TimeRecordDTO
import `in`.over.demo.application.dto.UpdateTimeRecordDTO
import `in`.over.demo.application.mapper.TimeRecordMapper
import `in`.over.demo.domain.service.TimeRecordService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/records")
class TimeRecordController(
    private val service: TimeRecordService,
    private val mapper: TimeRecordMapper,
) {
    @RequestMapping("/status")
    fun health() = "Hello!"

    @GetMapping
    fun listRecords(): List<TimeRecordDTO> = mapper.toDtos(service.getRecords())

    @DeleteMapping
    fun deleteRecord(@RequestParam id: Long): ResponseEntity<TimeRecordDTO> {
        val deleted = service.delete(id) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(mapper.toDto(deleted))
    }

    @PutMapping
    fun editRecord(
        @RequestParam id: Long,
        @RequestBody update: UpdateTimeRecordDTO,
    ): ResponseEntity<TimeRecordDTO> {
        val edited = service.update(id, update) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(mapper.toDto(edited))
    }


    @PostMapping("/time-in")
    fun timeIn(@RequestBody name: String): ResponseEntity<TimeRecordDTO> {
        val timedIn = service.timeIn(name)
        return ResponseEntity.ok(mapper.toDto(timedIn))
    }

    @PostMapping("/time-out")
    fun timeOut(@RequestBody name: String): ResponseEntity<TimeRecordDTO> {
        val timedOut = service.timeOut(name) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(mapper.toDto(timedOut))
    }
}
