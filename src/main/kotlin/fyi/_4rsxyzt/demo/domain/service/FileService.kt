package fyi._4rsxyzt.demo.domain.service

import fyi._4rsxyzt.demo.domain.model.StoredFile
import org.springframework.web.multipart.MultipartFile

interface FileService {
    fun getFiles(): List<StoredFile>
    fun getFile(id: Long): StoredFile?
    fun upload(file: MultipartFile): StoredFile
    fun upload(filename: String, contentType: String, content: ByteArray): StoredFile
    fun update(id: Long, file: MultipartFile): StoredFile?
    fun delete(id: Long): StoredFile?
}
