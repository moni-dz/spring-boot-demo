package `in`.over.demo.application.controller

import `in`.over.demo.application.dto.RoleDTO
import `in`.over.demo.application.dto.RoleWriteDTO
import `in`.over.demo.application.mapper.RoleMapper
import `in`.over.demo.domain.service.RoleService
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
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
class RoleController(
    private val service: RoleService,
    private val mapper: RoleMapper,
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
        return ResponseEntity.created(URI.create("/role/${role.id}")).body(mapper.toDto(role))
    }

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @Valid @RequestBody request: RoleWriteDTO): ResponseEntity<RoleDTO> {
        val role = service.update(id, request) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(mapper.toDto(role))
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<RoleDTO> {
        val role = try {
            service.delete(id)
        } catch (_: DataIntegrityViolationException) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build()
        } ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(mapper.toDto(role))
    }
}
