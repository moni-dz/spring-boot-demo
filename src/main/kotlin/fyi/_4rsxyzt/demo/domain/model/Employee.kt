package fyi._4rsxyzt.demo.domain.model

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.Table

@Entity
@Table(name = "employees")
class Employee(
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,
    @field:Column(name = "last_name", nullable = false, length = 100)
    var lastName: String = "",
    @field:Column(name = "first_name", nullable = false, length = 100)
    var firstName: String = "",
    @field:Column(name = "middle_name", length = 100)
    var middleName: String? = null,
    @field:Column(name = "username", nullable = false, length = 100, unique = true)
    var username: String = "",
    @field:Column(name = "email", nullable = false, length = 255, unique = true)
    var email: String = "",
    @field:Column(name = "password_hash", nullable = false, length = 255)
    var passwordHash: String = "",
    @field:ManyToMany(fetch = FetchType.LAZY, cascade = [CascadeType.PERSIST, CascadeType.MERGE])
    @field:JoinTable(
        name = "employee_roles",
        joinColumns = [JoinColumn(name = "employee_id")],
        inverseJoinColumns = [JoinColumn(name = "role_id")],
    )
    var roles: MutableSet<Role> = mutableSetOf(),
)
