package org.cubectl.identity.role

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/roles")
class RoleController(
    private val roleRepository: RoleRepository,
    private val permissionRepository: PermissionRepository,
) {

    @GetMapping
    @PreAuthorize("hasAuthority('PERMISSION_role_read') or hasAuthority('PERMISSION_role_manage')")
    fun roles(): List<RoleResponse> =
        roleRepository.findAll().map(RoleResponse::from)

    @PostMapping
    @PreAuthorize("hasAuthority('PERMISSION_role_manage')")
    fun create(@Valid @RequestBody request: CreateRoleRequest): ResponseEntity<RoleResponse> {
        if (roleRepository.existsByCode(request.code)) {
            throw IllegalArgumentException("Role already exists")
        }
        val role = RoleEntity(request.code, request.displayName, request.description, false)
        role.permissions.addAll(resolvePermissions(request.permissionCodes))
        return ResponseEntity.status(HttpStatus.CREATED).body(RoleResponse.from(roleRepository.save(role)))
    }

    @GetMapping("/{role_id}")
    @PreAuthorize("hasAuthority('PERMISSION_role_read') or hasAuthority('PERMISSION_role_manage')")
    fun role(@PathVariable("role_id") roleId: UUID): RoleResponse =
        roleRepository.findWithPermissionsById(roleId)
            .map(RoleResponse::from)
            .orElseThrow { IllegalArgumentException("Role not found") }

    @PatchMapping("/{role_id}")
    @PreAuthorize("hasAuthority('PERMISSION_role_manage')")
    fun update(
        @PathVariable("role_id") roleId: UUID,
        @Valid @RequestBody request: UpdateRoleRequest,
    ): RoleResponse {
        val role = roleRepository.findWithPermissionsById(roleId)
            .orElseThrow { IllegalArgumentException("Role not found") }
        role.update(request.displayName, request.description, resolvePermissions(request.permissionCodes))
        return RoleResponse.from(roleRepository.save(role))
    }

    @DeleteMapping("/{role_id}")
    @PreAuthorize("hasAuthority('PERMISSION_role_manage')")
    fun delete(@PathVariable("role_id") roleId: UUID): ResponseEntity<Void> {
        val role = roleRepository.findById(roleId)
            .orElseThrow { IllegalArgumentException("Role not found") }
        if (role.systemRole) {
            throw IllegalArgumentException("System role cannot be deleted")
        }
        roleRepository.delete(role)
        return ResponseEntity.noContent().build()
    }

    private fun resolvePermissions(codes: Set<String>): Set<PermissionEntity> {
        val permissions = permissionRepository.findByCodeIn(codes)
        if (permissions.size != codes.size) {
            throw IllegalArgumentException("Unknown permission code")
        }
        return permissions.toSet()
    }

    data class CreateRoleRequest(
        @field:NotBlank
        @field:Size(max = 64)
        val code: String,
        @field:NotBlank
        @field:Size(max = 128)
        val displayName: String,
        val description: String?,
        @field:NotNull
        val permissionCodes: Set<String>,
    )

    data class UpdateRoleRequest(
        @field:NotBlank
        @field:Size(max = 128)
        val displayName: String,
        val description: String?,
        @field:NotNull
        val permissionCodes: Set<String>,
    )

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
}
