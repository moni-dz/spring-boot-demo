package fyi._4rsxyzt.demo.application.mapper

import fyi._4rsxyzt.demo.application.dto.RoleDTO
import fyi._4rsxyzt.demo.application.dto.RoleWriteDTO
import fyi._4rsxyzt.demo.domain.model.Role
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingTarget

@Mapper(componentModel = "spring")
interface RoleMapper {
    fun toDto(role: Role): RoleDTO
    fun toDtos(roles: Collection<Role>): List<RoleDTO>

    @Mapping(target = "id", ignore = true)
    fun toModel(dto: RoleWriteDTO): Role

    @Mapping(target = "id", ignore = true)
    fun update(dto: RoleWriteDTO, @MappingTarget role: Role)
}
