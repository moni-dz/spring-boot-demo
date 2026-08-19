package `in`.over.demo.application.nats

import io.nats.client.Connection
import org.springframework.stereotype.Component

@Component
class NatsEventPublisher(private val connection: Connection) {
    fun publish(entity: String, action: String, id: Any?) {
        connection.publish("demo.$entity.$action", (id?.toString() ?: "").toByteArray())
    }
}
