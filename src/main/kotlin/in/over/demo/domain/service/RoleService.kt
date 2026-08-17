package `in`.over.demo.domain.service

import `in`.over.demo.application.dto.RoleWriteDTO
import `in`.over.demo.domain.model.Role

interface RoleService {
    fun getRoles(): List<Role>
    fun getRole(id: Long): Role?
    fun create(request: RoleWriteDTO): Role
    fun update(id: Long, request: RoleWriteDTO): Role?
    fun delete(id: Long): Role?
}
