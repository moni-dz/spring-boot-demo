package `in`.over.demo.domain.repository

import `in`.over.demo.domain.model.Role
import org.springframework.data.jpa.repository.JpaRepository

interface RoleRepository : JpaRepository<Role, Long>