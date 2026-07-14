package org.cubectl.identity.auth.dto

data class LogoutRequest(
    val refreshToken: String?,
)
