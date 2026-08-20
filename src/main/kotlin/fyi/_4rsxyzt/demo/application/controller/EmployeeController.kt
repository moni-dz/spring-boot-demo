package fyi._4rsxyzt.demo.application.controller

import fyi._4rsxyzt.demo.application.dto.EmployeeDTO
import fyi._4rsxyzt.demo.application.dto.EmployeeWriteDTO
import fyi._4rsxyzt.demo.application.mapper.EmployeeMapper
import fyi._4rsxyzt.demo.application.nats.NatsEventPublisher
import fyi._4rsxyzt.demo.domain.service.EmployeeService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
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
    @Operation(summary = "List employees", description = "Admin only.")
    fun list(): List<EmployeeDTO> = mapper.toDtos(service.getEmployees())

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get an employee by id", description = "Admin only.")
    @ApiResponse(responseCode = "404", description = "Employee not found")
    fun get(@PathVariable id: Long): ResponseEntity<EmployeeDTO> {
        val employee = service.getEmployee(id) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(mapper.toDto(employee))
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Create an employee",
        description = "Admin only. password is required on create; roleIds must reference existing roles.",
    )
    @ApiResponse(responseCode = "201", description = "Employee created")
    fun create(@Valid @RequestBody request: EmployeeWriteDTO): ResponseEntity<EmployeeDTO> {
        val employee = service.create(request)
        events.publish("employee", "created", employee.id)
        return ResponseEntity.created(URI.create("/employee/${employee.id}"))
            .body(mapper.toDto(employee))
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Update an employee", description = "Self or admin. Only admins may change roleIds.")
    @ApiResponse(responseCode = "404", description = "Employee not found")
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
    @Operation(summary = "Delete an employee", description = "Admin only.")
    @ApiResponse(responseCode = "404", description = "Employee not found")
    @ApiResponse(responseCode = "409", description = "Employee still referenced by payroll/time/file records")
    fun delete(@PathVariable id: Long): ResponseEntity<EmployeeDTO> {
        val employee = service.delete(id) ?: return ResponseEntity.notFound().build()
        events.publish("employee", "deleted", employee.id)
        return ResponseEntity.ok(mapper.toDto(employee))
    }
}
