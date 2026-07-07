package org.cubectl.identity.user

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
import java.util.UUID

interface UserRepository : JpaRepository<UserEntity, UUID> {

    fun existsByUsername(username: String): Boolean

    @EntityGraph(attributePaths = ["roles", "roles.permissions"])
    fun findWithRolesByUsername(username: String): Optional<UserEntity>

    @EntityGraph(attributePaths = ["roles", "roles.permissions"])
    fun findWithRolesById(id: UUID): Optional<UserEntity>
}
