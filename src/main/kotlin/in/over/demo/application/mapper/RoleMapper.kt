package `in`.over.demo.application.mapper

import `in`.over.demo.application.dto.RoleDTO
import `in`.over.demo.application.dto.RoleWriteDTO
import `in`.over.demo.domain.model.Role
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
