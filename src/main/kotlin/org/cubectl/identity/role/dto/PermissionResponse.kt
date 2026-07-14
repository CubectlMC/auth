package org.cubectl.identity.role.dto

import java.util.UUID

data class PermissionResponse(
    val id: UUID,
    val code: String,
    val description: String?,
)
