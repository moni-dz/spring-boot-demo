package `in`.over.demo

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import kotlin.time.Clock

@RestController
@RequestMapping("/records")
class ReqController(private val service: TimeRecordService) {
    @RequestMapping("/status")
    fun health() = "Hello!"

    @GetMapping

    fun listRecords() = service.records

    @DeleteMapping
    fun deleteRecord(@RequestParam id: Long): ResponseEntity<TimeRecord> {
        val recordToDelete = service.records.find { it.id == id }
        service.records.remove(recordToDelete)
        return ResponseEntity.ok(recordToDelete)
    }


    @PostMapping("/time-in")
    fun timeIn(@RequestBody name: String): ResponseEntity<TimeRecord> {
        val id = try {
            service.records.maxOf { it.id } + 1
        } catch (_: NoSuchElementException) {
            0
        }

        val record = service.insert(TimeRecord(id, name, Clock.System.now().epochSeconds, null))
        return ResponseEntity.ok(record)
    }

    @PostMapping("/time-out")
    fun timeOut(@RequestBody name: String): ResponseEntity<TimeRecord> {
        service.records.findLast { it.name == name && it.timeOutEpoch == null }?.let {
            it.timeOutEpoch = Clock.System.now().epochSeconds
            return ResponseEntity.ok(it)
        }

        return ResponseEntity.notFound().build()
    }
}