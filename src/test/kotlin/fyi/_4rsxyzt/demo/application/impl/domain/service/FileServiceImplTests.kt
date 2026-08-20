package fyi._4rsxyzt.demo.application.impl.domain.service

import fyi._4rsxyzt.demo.domain.repository.FileRepository
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verifyNoInteractions
import org.springframework.mock.web.MockMultipartFile
import software.amazon.awssdk.services.s3.S3Client
import kotlin.test.assertFailsWith

class FileServiceImplTests {
    private val repository = mock(FileRepository::class.java)
    private val s3 = mock(S3Client::class.java)
    private val service = FileServiceImpl(repository, s3, "test-bucket")

    @Test
    fun `upload rejects empty files before touching storage`() {
        val empty = MockMultipartFile("file", "empty.txt", "text/plain", ByteArray(0))

        assertFailsWith<IllegalArgumentException> { service.upload(empty) }
        verifyNoInteractions(s3, repository)
    }

    @Test
    fun `update rejects empty files before touching storage`() {
        val empty = MockMultipartFile("file", "empty.txt", "text/plain", ByteArray(0))

        assertFailsWith<IllegalArgumentException> { service.update(1, empty) }
        verifyNoInteractions(s3, repository)
    }
}
