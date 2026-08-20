package fyi._4rsxyzt.demo.application.controller

import fyi._4rsxyzt.demo.application.dto.FileDTO
import fyi._4rsxyzt.demo.application.dto.TimeRecordDTO
import fyi._4rsxyzt.demo.application.dto.UpdateTimeRecordDTO
import fyi._4rsxyzt.demo.application.mapper.FileMapper
import fyi._4rsxyzt.demo.application.mapper.TimeRecordMapper
import fyi._4rsxyzt.demo.application.nats.NatsEventPublisher
import fyi._4rsxyzt.demo.domain.service.TimeRecordService
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
@RequestMapping("/records")
@Tag(name = "Time Records")
class TimeRecordController(
    private val service: TimeRecordService,
    private val mapper: TimeRecordMapper,
    private val fileMapper: FileMapper,
    private val events: NatsEventPublisher,
) {
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ApiResponse(
        responseCode = "200",
        description = "Time records",
        content = [
            Content(
                mediaType = "application/json",
                array = ArraySchema(schema = Schema(implementation = TimeRecordDTO::class)),
            ),
        ],
    )
    fun listRecords(): List<TimeRecordDTO> = mapper.toDtos(service.getRecords())

    @PostMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    fun exportRecords(): ResponseEntity<FileDTO> {
        val stored = service.exportCsv()
        events.publish("file", "created", stored.id)
        return ResponseEntity.created(URI.create("/files/${stored.id}")).body(fileMapper.toDto(stored))
    }

    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun deleteRecord(@RequestParam id: Long): ResponseEntity<TimeRecordDTO> {
        val deleted = service.delete(id) ?: return ResponseEntity.notFound().build()
        events.publish("time-record", "deleted", deleted.id)
        return ResponseEntity.ok(mapper.toDto(deleted))
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun editRecord(
        @RequestParam id: Long,
        @Valid @RequestBody update: UpdateTimeRecordDTO,
    ): ResponseEntity<TimeRecordDTO> {
        val edited = service.update(id, update) ?: return ResponseEntity.notFound().build()
        events.publish("time-record", "updated", edited.id)
        return ResponseEntity.ok(mapper.toDto(edited))
    }


    @PostMapping("/time-in")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    fun timeIn(@RequestParam id: Long): ResponseEntity<TimeRecordDTO> {
        val timedIn = service.timeIn(id) ?: return ResponseEntity.notFound().build()
        events.publish("time-record", "created", timedIn.id)
        return ResponseEntity.ok(mapper.toDto(timedIn))
    }

    @PostMapping("/time-out")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    fun timeOut(@RequestParam id: Long): ResponseEntity<TimeRecordDTO> {
        val timedOut = service.timeOut(id) ?: return ResponseEntity.notFound().build()
        events.publish("time-record", "updated", timedOut.id)
        return ResponseEntity.ok(mapper.toDto(timedOut))
    }
}
