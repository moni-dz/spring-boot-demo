package fyi._4rsxyzt.demo.domain.repository

import fyi._4rsxyzt.demo.domain.model.Role
import org.springframework.data.jpa.repository.JpaRepository

interface RoleRepository : JpaRepository<Role, Long>