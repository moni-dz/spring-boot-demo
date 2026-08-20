package fyi._4rsxyzt.demo.application.impl.domain.service

import fyi._4rsxyzt.demo.BaseServiceTest
import fyi._4rsxyzt.demo.application.dto.RoleWriteDTO
import fyi._4rsxyzt.demo.domain.service.RoleService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@SpringBootTest
class RoleServiceTests : BaseServiceTest {
    @Autowired
    private lateinit var service: RoleService

    @Test
    fun `built-in roles cannot be deleted`() {
        assertFailsWith<IllegalArgumentException> { service.delete(1) } // ADMIN
        assertFailsWith<IllegalArgumentException> { service.delete(2) } // USER
        assertNotNull(service.getRole(1))
        assertNotNull(service.getRole(2))
    }

    @Test
    fun `custom role can be created, normalized, and deleted`() {
        val created = service.create(RoleWriteDTO(name = "  manager "))
        assertEquals("MANAGER", created.name)

        assertEquals(created.id, service.delete(created.id)?.id)
        assertNull(service.getRole(created.id))
    }
}
