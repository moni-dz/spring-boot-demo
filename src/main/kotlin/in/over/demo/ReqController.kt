package `in`.over.demo

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import kotlin.time.Clock

@RestController
@RequestMapping("/records")
class ReqController {
    @RequestMapping("/status")
    fun health() = "Hello!"

    @GetMapping
    fun listRecords() = listOf(
        TimeRecord(1,"Lythe Marvin Lacre", Clock.System.now().epochSeconds, null),
    )
}