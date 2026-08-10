package `in`.over.demo

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/records")
class ReqController(private val service: TimeRecordService) {
    @RequestMapping("/status")
    fun health() = "Hello!"

    @GetMapping
    fun listRecords() = service.records
}