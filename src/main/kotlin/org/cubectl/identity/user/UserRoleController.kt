package org.cubectl.identity.user

import org.cubectl.identity.role.RoleController.RoleResponse
import org.cubectl.identity.role.RoleRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/users/{user_id}/roles")
class UserRoleController(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
) {

    @GetMapping
    @PreAuthorize("hasAuthority('PERMISSION_user_read') or hasAuthority('PERMISSION_user_manage')")
    fun roles(@PathVariable("user_id") userId: UUID): List<RoleResponse> {
        val user = userRepository.findWithRolesById(userId)
            .orElseThrow { IllegalArgumentException("User not found") }
        return user.roles.map(RoleResponse::from)
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PERMISSION_user_manage')")
    fun addRole(
        @PathVariable("user_id") userId: UUID,
        @RequestBody request: AddUserRoleRequest,
    ): ResponseEntity<List<RoleResponse>> {
        val user = userRepository.findWithRolesById(userId)
            .orElseThrow { IllegalArgumentException("User not found") }
        val role = roleRepository.findWithPermissionsById(request.roleId)
            .orElseThrow { IllegalArgumentException("Role not found") }
        user.roles.add(role)
        userRepository.save(user)
        return ResponseEntity.status(HttpStatus.CREATED).body(user.roles.map(RoleResponse::from))
    }

    @DeleteMapping("/{role_id}")
    @PreAuthorize("hasAuthority('PERMISSION_user_manage')")
    fun deleteRole(
        @PathVariable("user_id") userId: UUID,
        @PathVariable("role_id") roleId: UUID,
    ): ResponseEntity<Void> {
        val user = userRepository.findWithRolesById(userId)
            .orElseThrow { IllegalArgumentException("User not found") }
        user.roles.removeIf { role -> role.id == roleId }
        userRepository.save(user)
        return ResponseEntity.noContent().build()
    }

    data class AddUserRoleRequest(
        val roleId: UUID,
    )
}
