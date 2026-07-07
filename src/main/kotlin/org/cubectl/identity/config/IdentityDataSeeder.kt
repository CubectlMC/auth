package org.cubectl.identity.config

import org.cubectl.identity.role.PermissionEntity
import org.cubectl.identity.role.PermissionRepository
import org.cubectl.identity.role.RoleEntity
import org.cubectl.identity.role.RoleRepository
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class IdentityDataSeeder(
    private val permissionRepository: PermissionRepository,
    private val roleRepository: RoleRepository,
) : ApplicationRunner {

    @Transactional
    override fun run(args: ApplicationArguments) {
        for (code in permissions) {
            permissionRepository.findByCode(code)
                .orElseGet { permissionRepository.save(PermissionEntity(code, code)) }
        }

        val permissionByCode = permissionRepository.findAll().associateBy { permission -> permission.code }

        createRole("admin", "Admin", "Full access", permissionByCode.keys, permissionByCode)
        createRole(
            code = "instance_creator",
            displayName = "Instance creator",
            description = "Can create and read instances",
            permissionCodes = setOf("instance_create", "instance_read"),
            permissions = permissionByCode,
        )
        createRole(
            code = "viewer",
            displayName = "Viewer",
            description = "Read-only access",
            permissionCodes = setOf(
                "user_read",
                "role_read",
                "permission_read",
                "instance_read",
                "instance_content_read",
                "instance_logs_read",
                "instance_live_logs_read",
            ),
            permissions = permissionByCode,
        )
    }

    private fun createRole(
        code: String,
        displayName: String,
        description: String,
        permissionCodes: Set<String>,
        permissions: Map<String, PermissionEntity>,
    ) {
        roleRepository.findByCode(code).orElseGet {
            val role = RoleEntity(code, displayName, description, true)
            role.permissions.addAll(permissionCodes.mapNotNull(permissions::get))
            roleRepository.save(role)
        }
    }

    companion object {
        private val permissions = setOf(
            "user_read",
            "user_manage",
            "role_read",
            "role_manage",
            "permission_read",
            "instance_create",
            "instance_read",
            "instance_update",
            "instance_delete",
            "instance_start",
            "instance_stop",
            "instance_restart",
            "instance_runtime_update",
            "instance_java_version_update",
            "instance_member_manage",
            "instance_content_read",
            "instance_content_update",
            "instance_logs_read",
            "instance_live_logs_read",
            "resource_limits_read",
            "resource_limits_manage",
        )
    }
}
