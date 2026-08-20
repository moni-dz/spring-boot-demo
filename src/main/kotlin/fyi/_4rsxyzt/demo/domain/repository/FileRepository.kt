package fyi._4rsxyzt.demo.domain.repository

import fyi._4rsxyzt.demo.domain.model.StoredFile
import org.springframework.data.jpa.repository.JpaRepository

interface FileRepository : JpaRepository<StoredFile, Long>
