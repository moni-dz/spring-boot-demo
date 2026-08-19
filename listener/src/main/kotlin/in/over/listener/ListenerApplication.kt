package `in`.over.listener

import io.nats.client.Connection
import io.nats.client.Nats
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets

private val log = LoggerFactory.getLogger("in.over.listener")

@SpringBootApplication
class ListenerApplication

fun main(args: Array<String>) {
    runApplication<ListenerApplication>(*args)
}

@Configuration
class NatsConfig {
    @Bean(destroyMethod = "close")
    fun natsConnection(@Value("\${nats.url}") url: String): Connection = Nats.connect(url)
}

@Component
class DemoEventSubscriber(connection: Connection) {
    init {
        val dispatcher = connection.createDispatcher { msg ->
            log.info("event on '{}': {}", msg.subject, String(msg.data, StandardCharsets.UTF_8))
        }
        dispatcher.subscribe("demo.>")
    }
}
