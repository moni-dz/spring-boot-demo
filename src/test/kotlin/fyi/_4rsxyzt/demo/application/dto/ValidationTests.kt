package fyi._4rsxyzt.demo.application.dto

import jakarta.validation.Validation
import org.junit.jupiter.api.Test
import java.time.Instant
import kotlin.test.assertTrue

class ValidationTests {
    private val validator = Validation.buildDefaultValidatorFactory().validator

    @Test
    fun `role name must not be blank`() {
        assertTrue(validator.validate(RoleWriteDTO(name = "  ")).isNotEmpty())
        assertTrue(validator.validate(RoleWriteDTO(name = "manager")).isEmpty())
    }

    @Test
    fun `time record timeOut must be after timeIn`() {
        val invalid = UpdateTimeRecordDTO(
            timeIn = Instant.parse("2026-01-01T10:00:00Z"),
            timeOut = Instant.parse("2026-01-01T09:00:00Z"),
        )
        assertTrue(validator.validate(invalid).isNotEmpty())

        val valid = UpdateTimeRecordDTO(
            timeIn = Instant.parse("2026-01-01T09:00:00Z"),
            timeOut = Instant.parse("2026-01-01T10:00:00Z"),
        )
        assertTrue(validator.validate(valid).isEmpty())

        val partial = UpdateTimeRecordDTO(timeIn = null, timeOut = Instant.parse("2026-01-01T10:00:00Z"))
        assertTrue(validator.validate(partial).isEmpty())
    }
}
