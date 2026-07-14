package org.cubectl.identity.auth.dto

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,
)
