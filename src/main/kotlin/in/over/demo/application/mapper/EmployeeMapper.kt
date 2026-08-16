package `in`.over.demo.application.mapper

import `in`.over.demo.application.dto.EmployeeDTO
import `in`.over.demo.application.dto.EmployeeWriteDTO
import `in`.over.demo.domain.model.Employee
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingTarget

@Mapper(componentModel = "spring")
interface EmployeeMapper {
    fun toDto(employee: Employee): EmployeeDTO
    fun toDtos(employees: List<Employee>): List<EmployeeDTO>

    @Mapping(target = "id", ignore = true)
    fun toModel(dto: EmployeeWriteDTO): Employee

    @Mapping(target = "id", ignore = true)
    fun update(dto: EmployeeWriteDTO, @MappingTarget employee: Employee)
}
