package fyi._4rsxyzt.demo.domain.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "files")
class StoredFile(
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,
    @field:Column(name = "object_key", nullable = false, length = 255, unique = true)
    var objectKey: String = "",
    @field:Column(name = "original_filename", nullable = false, length = 255)
    var originalFilename: String = "",
    @field:Column(name = "content_type", nullable = false, length = 127)
    var contentType: String = "",
    @field:Column(name = "size_bytes", nullable = false)
    var sizeBytes: Long = 0,
    @field:Column(name = "uploaded_by", nullable = false)
    var uploadedBy: Long = 0,
    @field:Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant = Instant.now(),
    @field:Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now(),
)
