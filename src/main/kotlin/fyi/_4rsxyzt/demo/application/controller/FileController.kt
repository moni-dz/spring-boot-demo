package fyi._4rsxyzt.demo.application.controller

import fyi._4rsxyzt.demo.application.dto.FileDTO
import fyi._4rsxyzt.demo.application.mapper.FileMapper
import fyi._4rsxyzt.demo.application.nats.NatsEventPublisher
import fyi._4rsxyzt.demo.domain.service.FileService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.net.URI

@RestController
@RequestMapping("/files")
@Tag(name = "Files")
class FileController(
    private val service: FileService,
    private val mapper: FileMapper,
    private val events: NatsEventPublisher,
) {
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List files", description = "Admin only.")
    fun list(): List<FileDTO> = mapper.toDtos(service.getFiles())

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Get file metadata by id", description = "Uploader or admin.")
    @ApiResponse(responseCode = "404", description = "File not found")
    fun get(@PathVariable id: Long): ResponseEntity<FileDTO> {
        val stored = service.getFile(id) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(mapper.toDto(stored))
    }

    @PostMapping(consumes = ["multipart/form-data"])
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Upload a file")
    @ApiResponse(responseCode = "201", description = "File uploaded")
    @ApiResponse(responseCode = "400", description = "File is empty")
    fun upload(@RequestParam("file") file: MultipartFile): ResponseEntity<FileDTO> {
        val stored = service.upload(file)
        events.publish("file", "created", stored.id)
        return ResponseEntity.created(URI.create("/files/${stored.id}")).body(mapper.toDto(stored))
    }

    @PutMapping("/{id}", consumes = ["multipart/form-data"])
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Replace a file's contents", description = "Uploader or admin.")
    @ApiResponse(responseCode = "400", description = "File is empty")
    @ApiResponse(responseCode = "404", description = "File not found")
    fun update(@PathVariable id: Long, @RequestParam("file") file: MultipartFile): ResponseEntity<FileDTO> {
        val stored = service.update(id, file) ?: return ResponseEntity.notFound().build()
        events.publish("file", "updated", stored.id)
        return ResponseEntity.ok(mapper.toDto(stored))
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Delete a file", description = "Uploader or admin.")
    @ApiResponse(responseCode = "404", description = "File not found")
    fun delete(@PathVariable id: Long): ResponseEntity<FileDTO> {
        val stored = service.delete(id) ?: return ResponseEntity.notFound().build()
        events.publish("file", "deleted", stored.id)
        return ResponseEntity.ok(mapper.toDto(stored))
    }
}
