package org.cubectl.identity.role

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/permissions")
class PermissionController(
    private val permissionRepository: PermissionRepository,
) {

    @GetMapping
    @PreAuthorize("hasAuthority('PERMISSION_permission_read')")
    fun permissions(): List<PermissionResponse> =
        permissionRepository.findAll()
            .map { permission -> PermissionResponse(permission.id, permission.code, permission.description) }

    data class PermissionResponse(
        val id: UUID,
        val code: String,
        val description: String?,
    )
}
