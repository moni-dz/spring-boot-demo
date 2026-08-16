package `in`.over.demo

import com.github.database.rider.core.api.configuration.DBUnit
import com.github.database.rider.core.api.dataset.DataSet
import com.github.database.rider.spring.api.DBRider
import org.springframework.context.annotation.Import

@DBRider
@DBUnit(caseSensitiveTableNames = true, raiseExceptionOnCleanUp = true)
@DataSet(value = ["datasets/employees.yml"], cleanBefore = true)
@Import(MySqlTestConfiguration::class)
interface BaseServiceTest