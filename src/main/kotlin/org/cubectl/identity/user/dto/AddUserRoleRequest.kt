package org.cubectl.identity.user.dto

import java.util.UUID

data class AddUserRoleRequest(
    val roleId: UUID,
)
