package `in`.over.demo.domain.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "roles")
class Role(
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,
    @field:Column(name = "name", nullable = false, length = 50, unique = true)
    var name: String = "",
) {
    companion object {
        const val ADMIN = "ADMIN"
        const val USER = "USER"
    }
}
