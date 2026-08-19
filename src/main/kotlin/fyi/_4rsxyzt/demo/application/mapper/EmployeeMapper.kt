package fyi._4rsxyzt.demo.application.mapper

import fyi._4rsxyzt.demo.application.dto.EmployeeDTO
import fyi._4rsxyzt.demo.application.dto.EmployeeWriteDTO
import fyi._4rsxyzt.demo.domain.model.Employee
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingTarget

@Mapper(componentModel = "spring", uses = [RoleMapper::class])
interface EmployeeMapper {
    fun toDto(employee: Employee): EmployeeDTO
    fun toDtos(employees: List<Employee>): List<EmployeeDTO>

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "roles", ignore = true)
    fun toModel(dto: EmployeeWriteDTO): Employee

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "roles", ignore = true)
    fun update(dto: EmployeeWriteDTO, @MappingTarget employee: Employee)
}
