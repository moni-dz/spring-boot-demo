package `in`.over.demo.application.controller

import `in`.over.demo.application.dto.EmployeeDTO
import `in`.over.demo.application.dto.EmployeeWriteDTO
import `in`.over.demo.application.mapper.EmployeeMapper
import `in`.over.demo.application.nats.NatsEventPublisher
import `in`.over.demo.domain.service.EmployeeService
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
@RequestMapping("/employee")
@Tag(name = "Employees")
class EmployeeController(
    private val service: EmployeeService,
    private val mapper: EmployeeMapper,
    private val events: NatsEventPublisher,
) {
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun list(): List<EmployeeDTO> = mapper.toDtos(service.getEmployees())

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun get(@PathVariable id: Long): ResponseEntity<EmployeeDTO> {
        val employee = service.getEmployee(id) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(mapper.toDto(employee))
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun create(@Valid @RequestBody request: EmployeeWriteDTO): ResponseEntity<EmployeeDTO> {
        val employee = service.create(request)
        events.publish("employee", "created", employee.id)
        return ResponseEntity.created(URI.create("/employee/${employee.id}"))
            .body(mapper.toDto(employee))
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody request: EmployeeWriteDTO,
    ): ResponseEntity<EmployeeDTO> {
        val employee = service.update(id, request) ?: return ResponseEntity.notFound().build()
        events.publish("employee", "updated", employee.id)
        return ResponseEntity.ok(mapper.toDto(employee))
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun delete(@PathVariable id: Long): ResponseEntity<EmployeeDTO> {
        val employee = try {
            service.delete(id)
        } catch (_: DataIntegrityViolationException) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build()
        } ?: return ResponseEntity.notFound().build()

        events.publish("employee", "deleted", employee.id)
        return ResponseEntity.ok(mapper.toDto(employee))
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun invalidRequest(): ResponseEntity<Void> = ResponseEntity.badRequest().build()
}
