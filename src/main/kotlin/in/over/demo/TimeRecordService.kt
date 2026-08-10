package `in`.over.demo

import org.springframework.stereotype.Service

@Service
class TimeRecordService {
    val records = mutableListOf<TimeRecord>()
}