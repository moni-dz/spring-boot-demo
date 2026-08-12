package `in`.over.demo

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@OpenAPIDefinition(
	info = Info(
		title = "Time Record API",
		description = "API for recording employee time in and time out events",
		version = "v1",
	),
)
@SpringBootApplication
class TimeRecordApplication

fun main(args: Array<String>) {
	runApplication<TimeRecordApplication>(*args)
}
