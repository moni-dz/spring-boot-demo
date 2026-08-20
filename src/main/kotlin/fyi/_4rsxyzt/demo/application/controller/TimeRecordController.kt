package fyi._4rsxyzt.demo.application.controller

import fyi._4rsxyzt.demo.application.dto.FileDTO
import fyi._4rsxyzt.demo.application.dto.TimeRecordDTO
import fyi._4rsxyzt.demo.application.dto.UpdateTimeRecordDTO
import fyi._4rsxyzt.demo.application.mapper.FileMapper
import fyi._4rsxyzt.demo.application.mapper.TimeRecordMapper
import fyi._4rsxyzt.demo.application.nats.NatsEventPublisher
import fyi._4rsxyzt.demo.domain.service.TimeRecordService
import io.swagger.v3.oas.annotations.Operation
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
    @Operation(
        summary = "Export all time records as CSV",
        description = "Uploads the CSV to file storage and returns its metadata.",
    )
    @ApiResponse(responseCode = "201", description = "CSV file created")
    fun exportRecords(): ResponseEntity<FileDTO> {
        val stored = service.exportCsv()
        events.publish("file", "created", stored.id)
        return ResponseEntity.created(URI.create("/files/${stored.id}")).body(fileMapper.toDto(stored))
    }

    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a time record")
    @ApiResponse(responseCode = "404", description = "Time record not found")
    fun deleteRecord(@RequestParam id: Long): ResponseEntity<TimeRecordDTO> {
        val deleted = service.delete(id) ?: return ResponseEntity.notFound().build()
        events.publish("time-record", "deleted", deleted.id)
        return ResponseEntity.ok(mapper.toDto(deleted))
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Edit a time record", description = "timeOut must be after timeIn when both are present.")
    @ApiResponse(responseCode = "404", description = "Time record not found")
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
    @Operation(summary = "Clock in an employee", description = "Self or admin.")
    @ApiResponse(responseCode = "404", description = "Employee not found")
    fun timeIn(@RequestParam id: Long): ResponseEntity<TimeRecordDTO> {
        val timedIn = service.timeIn(id) ?: return ResponseEntity.notFound().build()
        events.publish("time-record", "created", timedIn.id)
        return ResponseEntity.ok(mapper.toDto(timedIn))
    }

    @PostMapping("/time-out")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Clock out an employee", description = "Self or admin. Closes the latest open record.")
    @ApiResponse(responseCode = "404", description = "No open time record for this employee")
    fun timeOut(@RequestParam id: Long): ResponseEntity<TimeRecordDTO> {
        val timedOut = service.timeOut(id) ?: return ResponseEntity.notFound().build()
        events.publish("time-record", "updated", timedOut.id)
        return ResponseEntity.ok(mapper.toDto(timedOut))
    }
}
