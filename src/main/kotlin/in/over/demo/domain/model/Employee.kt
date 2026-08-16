package `in`.over.demo.domain.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "employees")
data class Employee(
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,
    @field:Column(name = "last_name", nullable = false, length = 100)
    var lastName: String = "",
    @field:Column(name = "first_name", nullable = false, length = 100)
    var firstName: String = "",
    @field:Column(name = "middle_name", length = 100)
    var middleName: String? = null,
)
