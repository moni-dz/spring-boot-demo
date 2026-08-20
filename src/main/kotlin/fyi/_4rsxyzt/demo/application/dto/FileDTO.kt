package fyi._4rsxyzt.demo.application.dto

import java.time.Instant

data class FileDTO(
    val id: Long,
    val originalFilename: String,
    val contentType: String,
    val sizeBytes: Long,
    val uploadedBy: Long,
    val createdAt: Instant,
    val updatedAt: Instant,
)
