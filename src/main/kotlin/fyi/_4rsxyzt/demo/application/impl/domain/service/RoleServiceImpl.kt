package fyi._4rsxyzt.demo.application.impl.domain.service

import fyi._4rsxyzt.demo.application.dto.RoleWriteDTO
import fyi._4rsxyzt.demo.application.mapper.RoleMapper
import fyi._4rsxyzt.demo.domain.model.Role
import fyi._4rsxyzt.demo.domain.repository.RoleRepository
import fyi._4rsxyzt.demo.domain.service.RoleService
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RoleServiceImpl(
    private val repository: RoleRepository,
    private val mapper: RoleMapper,
) : RoleService {
    override fun getRoles(): List<Role> = repository.findAll(Sort.by("id"))

    override fun getRole(id: Long): Role? = repository.findById(id).orElse(null)

    @Transactional
    override fun create(request: RoleWriteDTO): Role = repository.save(mapper.toModel(normalize(request)))

    @Transactional
    override fun update(id: Long, request: RoleWriteDTO): Role? {
        val role = getRole(id) ?: return null
        mapper.update(normalize(request), role)
        return repository.save(role)
    }

    @Transactional
    override fun delete(id: Long): Role? {
        val role = getRole(id) ?: return null
        repository.delete(role)
        repository.flush()
        return role
    }

    private fun normalize(request: RoleWriteDTO): RoleWriteDTO = request.copy(name = request.name.trim().uppercase())
}
