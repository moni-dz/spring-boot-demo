package fyi._4rsxyzt.demo.domain.service

import fyi._4rsxyzt.demo.application.dto.RoleWriteDTO
import fyi._4rsxyzt.demo.domain.model.Role

interface RoleService {
    fun getRoles(): List<Role>
    fun getRole(id: Long): Role?
    fun create(request: RoleWriteDTO): Role
    fun update(id: Long, request: RoleWriteDTO): Role?
    fun delete(id: Long): Role?
}
