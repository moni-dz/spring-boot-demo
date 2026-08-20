package fyi._4rsxyzt.demo.application.nats

import io.nats.client.Connection
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class NatsEventPublisher(private val connection: Connection) {
    private val log = LoggerFactory.getLogger(NatsEventPublisher::class.java)

    fun publish(entity: String, action: String, id: Any?) {
        try {
            connection.publish("demo.$entity.$action", (id?.toString() ?: "").toByteArray())
        } catch (e: Exception) {
            log.error("failed to publish demo.$entity.$action event for id=$id", e)
        }
    }
}
