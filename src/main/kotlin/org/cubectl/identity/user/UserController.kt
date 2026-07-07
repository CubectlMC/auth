package org.cubectl.identity.user

import jakarta.validation.Valid
import jakarta.validation.constraints.Size
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val userRepository: UserRepository,
) {

    @GetMapping
    @PreAuthorize("hasAuthority('PERMISSION_user_read') or hasAuthority('PERMISSION_user_manage')")
    fun users(): List<UserResponse> =
        userRepository.findAll().map(UserResponse::from)

    @GetMapping("/{user_id}")
    @PreAuthorize("hasAuthority('PERMISSION_user_read') or hasAuthority('PERMISSION_user_manage')")
    fun user(@PathVariable("user_id") userId: UUID): UserResponse =
        userRepository.findById(userId)
            .map(UserResponse::from)
            .orElseThrow { IllegalArgumentException("User not found") }

    @PatchMapping("/{user_id}")
    @PreAuthorize("hasAuthority('PERMISSION_user_manage')")
    fun update(
        @PathVariable("user_id") userId: UUID,
        @Valid @RequestBody request: UpdateUserRequest,
    ): UserResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found") }
        request.username?.let { user.username = it }
        request.status?.let { user.setStatusWithDeletedAt(it) }
        return UserResponse.from(userRepository.save(user))
    }

    @DeleteMapping("/{user_id}")
    @PreAuthorize("hasAuthority('PERMISSION_user_manage')")
    fun delete(@PathVariable("user_id") userId: UUID): ResponseEntity<Void> {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found") }
        user.setStatusWithDeletedAt(UserStatus.DELETED)
        userRepository.save(user)
        return ResponseEntity.noContent().build()
    }

    data class UpdateUserRequest(
        @field:Size(min = 3, max = 64)
        val username: String?,
        val status: UserStatus?,
    )

    data class UserResponse(
        val id: UUID,
        val username: String,
        val status: UserStatus,
    ) {
        companion object {
            fun from(user: UserEntity): UserResponse =
                UserResponse(user.id, user.username, user.status)
        }
    }
}
