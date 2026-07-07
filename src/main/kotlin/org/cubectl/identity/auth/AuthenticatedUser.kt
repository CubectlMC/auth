package org.cubectl.identity.auth

import java.util.UUID

data class AuthenticatedUser(
    val userId: UUID,
    val username: String,
    val permissions: Set<String>,
)
