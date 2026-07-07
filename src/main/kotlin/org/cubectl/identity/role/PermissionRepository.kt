package org.cubectl.identity.role

import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
import java.util.UUID

interface PermissionRepository : JpaRepository<PermissionEntity, UUID> {

    fun findByCode(code: String): Optional<PermissionEntity>

    fun findByCodeIn(codes: Set<String>): List<PermissionEntity>
}
