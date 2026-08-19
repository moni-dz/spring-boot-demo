package fyi._4rsxyzt.demo

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.testcontainers.mysql.MySQLContainer

@TestConfiguration(proxyBeanMethods = false)
class MySqlTestConfiguration {
    @Bean
    @ServiceConnection
    fun mysqlContainer() = MySQLContainer("mysql:9.7.1")
}
