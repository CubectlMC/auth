package org.cubectl.identity.user.dto

import org.cubectl.identity.user.UserEntity
import org.cubectl.identity.user.UserStatus
import java.util.UUID

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
