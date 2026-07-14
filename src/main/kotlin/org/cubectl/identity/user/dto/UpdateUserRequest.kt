package org.cubectl.identity.user.dto

import jakarta.validation.constraints.Size
import org.cubectl.identity.user.UserStatus

data class UpdateUserRequest(
    @field:Size(min = 3, max = 64)
    val username: String?,
    val status: UserStatus?,
)
