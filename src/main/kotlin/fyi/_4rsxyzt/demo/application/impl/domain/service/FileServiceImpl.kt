package fyi._4rsxyzt.demo.application.impl.domain.service

import fyi._4rsxyzt.demo.application.security.CurrentEmployee
import fyi._4rsxyzt.demo.domain.model.StoredFile
import fyi._4rsxyzt.demo.domain.repository.FileRepository
import fyi._4rsxyzt.demo.domain.service.FileService
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Sort
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.CreateBucketRequest
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.HeadBucketRequest
import software.amazon.awssdk.services.s3.model.NoSuchBucketException
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.time.Instant
import java.util.UUID

@Service
class FileServiceImpl(
    private val repository: FileRepository,
    private val s3: S3Client,
    @Value($$"${minio.bucket-name}") private val bucket: String,
) : FileService {
    init {
        ensureBucket()
    }

    override fun getFiles(): List<StoredFile> = repository.findAll(Sort.by("id"))

    override fun getFile(id: Long): StoredFile? {
        val existing = repository.findById(id).orElse(null) ?: return null
        requireOwnerOrAdmin(existing.uploadedBy)
        return existing
    }

    override fun upload(file: MultipartFile): StoredFile {
        val key = "${UUID.randomUUID()}${extensionOf(file.originalFilename)}"
        putObject(key, file.contentType, RequestBody.fromInputStream(file.inputStream, file.size))
        return saveStored(key, file.originalFilename ?: key, file.contentType ?: "application/octet-stream", file.size)
    }

    override fun upload(filename: String, contentType: String, content: ByteArray): StoredFile {
        val key = "${UUID.randomUUID()}${extensionOf(filename)}"
        putObject(key, contentType, RequestBody.fromBytes(content))
        return saveStored(key, filename, contentType, content.size.toLong())
    }

    private fun saveStored(key: String, filename: String, contentType: String, size: Long): StoredFile {
        val ownerId = CurrentEmployee.get()?.id ?: throw AccessDeniedException("authentication required")

        val stored = StoredFile(
            objectKey = key,
            originalFilename = filename,
            contentType = contentType,
            sizeBytes = size,
            uploadedBy = ownerId,
        )

        return repository.save(stored)
    }

    override fun update(id: Long, file: MultipartFile): StoredFile? {
        val existing = repository.findById(id).orElse(null) ?: return null
        requireOwnerOrAdmin(existing.uploadedBy)

        putObject(existing.objectKey, file.contentType, RequestBody.fromInputStream(file.inputStream, file.size))
        existing.originalFilename = file.originalFilename ?: existing.originalFilename
        existing.contentType = file.contentType ?: existing.contentType
        existing.sizeBytes = file.size
        existing.updatedAt = Instant.now()

        return repository.save(existing)
    }

    override fun delete(id: Long): StoredFile? {
        val existing = repository.findById(id).orElse(null) ?: return null
        requireOwnerOrAdmin(existing.uploadedBy)

        s3.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(existing.objectKey).build())
        repository.delete(existing)
        return existing
    }

    private fun requireOwnerOrAdmin(ownerId: Long) = CurrentEmployee.requireSelfOrAdmin(ownerId, "file")

    private fun putObject(key: String, contentType: String?, body: RequestBody) {
        s3.putObject(
            PutObjectRequest.builder().bucket(bucket).key(key).contentType(contentType).build(),
            body,
        )
    }

    private fun extensionOf(filename: String?): String =
        filename?.substringAfterLast('.', "")?.takeIf { it.isNotEmpty() }?.let { ".$it" } ?: ""

    private fun ensureBucket() {
        try {
            s3.headBucket(HeadBucketRequest.builder().bucket(bucket).build())
        } catch (_: NoSuchBucketException) {
            s3.createBucket(CreateBucketRequest.builder().bucket(bucket).build())
        }
    }
}
