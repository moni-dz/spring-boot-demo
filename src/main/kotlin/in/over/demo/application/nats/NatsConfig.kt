package `in`.over.demo.application.nats

import io.nats.client.Connection
import io.nats.client.Nats
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class NatsConfig {
    @Bean(destroyMethod = "close")
    fun natsConnection(@Value("\${nats.url}") url: String): Connection = Nats.connect(url)
}
