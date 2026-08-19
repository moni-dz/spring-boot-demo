package fyi._4rsxyzt.demo.application.controller

import fyi._4rsxyzt.demo.application.dto.RoleDTO
import fyi._4rsxyzt.demo.application.dto.RoleWriteDTO
import fyi._4rsxyzt.demo.application.mapper.RoleMapper
import fyi._4rsxyzt.demo.application.nats.NatsEventPublisher
import fyi._4rsxyzt.demo.domain.service.RoleService
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
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
@RequestMapping("/role")
@Tag(name = "Roles")
@PreAuthorize("hasRole('ADMIN')")
class RoleController(
    private val service: RoleService,
    private val mapper: RoleMapper,
    private val events: NatsEventPublisher,
) {
    @GetMapping
    fun list(): List<RoleDTO> = mapper.toDtos(service.getRoles())

    @GetMapping("/{id}")
    fun get(@PathVariable id: Long): ResponseEntity<RoleDTO> {
        val role = service.getRole(id) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(mapper.toDto(role))
    }

    @PostMapping
    fun create(@Valid @RequestBody request: RoleWriteDTO): ResponseEntity<RoleDTO> {
        val role = service.create(request)
        events.publish("role", "created", role.id)
        return ResponseEntity.created(URI.create("/role/${role.id}")).body(mapper.toDto(role))
    }

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @Valid @RequestBody request: RoleWriteDTO): ResponseEntity<RoleDTO> {
        val role = service.update(id, request) ?: return ResponseEntity.notFound().build()
        events.publish("role", "updated", role.id)
        return ResponseEntity.ok(mapper.toDto(role))
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<RoleDTO> {
        val role = try {
            service.delete(id)
        } catch (_: DataIntegrityViolationException) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build()
        } ?: return ResponseEntity.notFound().build()

        events.publish("role", "deleted", role.id)
        return ResponseEntity.ok(mapper.toDto(role))
    }
}
