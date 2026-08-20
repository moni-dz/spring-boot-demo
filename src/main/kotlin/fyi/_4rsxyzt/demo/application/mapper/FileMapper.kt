package fyi._4rsxyzt.demo.application.mapper

import fyi._4rsxyzt.demo.application.dto.FileDTO
import fyi._4rsxyzt.demo.domain.model.StoredFile
import org.mapstruct.Mapper

@Mapper(componentModel = "spring")
interface FileMapper {
    fun toDto(file: StoredFile): FileDTO
    fun toDtos(files: List<StoredFile>): List<FileDTO>
}
