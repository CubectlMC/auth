package org.cubectl.identity.auth.dto

import java.util.UUID

data class MeResponse(
    val userId: UUID,
    val username: String,
    val permissions: Set<String>,
)
