package org.cubectl.identity.auth.dto

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,
) {
    companion object {
        fun from(token: TokenResponse): AuthResponse =
            AuthResponse(token.accessToken, token.refreshToken, token.tokenType)
    }
}
