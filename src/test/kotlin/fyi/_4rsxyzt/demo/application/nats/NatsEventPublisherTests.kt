package fyi._4rsxyzt.demo.application.nats

import io.nats.client.Connection
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.mock

class NatsEventPublisherTests {
    @Test
    fun `publish swallows connection failures instead of propagating`() {
        val connection = mock(Connection::class.java)
        doThrow(RuntimeException("nats down")).`when`(connection).publish(anyString(), any())

        NatsEventPublisher(connection).publish("employee", "created", 1L)
    }
}
