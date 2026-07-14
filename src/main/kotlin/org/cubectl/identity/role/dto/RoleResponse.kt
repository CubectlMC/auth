package org.cubectl.identity.role.dto

import org.cubectl.identity.role.PermissionEntity
import org.cubectl.identity.role.RoleEntity
import java.util.UUID

data class RoleResponse(
    val id: UUID,
    val code: String,
    val displayName: String,
    val description: String?,
    val permissionCodes: Set<String>,
) {
    companion object {
        fun from(role: RoleEntity): RoleResponse =
            RoleResponse(
                id = role.id,
                code = role.code,
                displayName = role.displayName,
                description = role.description,
                permissionCodes = role.permissions.map(PermissionEntity::code).toSet(),
            )
    }
}
