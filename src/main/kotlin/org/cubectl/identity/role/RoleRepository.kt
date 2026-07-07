package org.cubectl.identity.role

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
import java.util.UUID

interface RoleRepository : JpaRepository<RoleEntity, UUID> {

    fun existsByCode(code: String): Boolean

    @EntityGraph(attributePaths = ["permissions"])
    fun findByCode(code: String): Optional<RoleEntity>

    @EntityGraph(attributePaths = ["permissions"])
    fun findWithPermissionsById(id: UUID): Optional<RoleEntity>
}
