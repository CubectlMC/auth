package org.cubectl.identity.auth

import org.cubectl.identity.token.RefreshTokenEntity
import org.cubectl.identity.token.RefreshTokenRepository
import org.cubectl.identity.user.UserEntity
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.Instant
import java.util.HexFormat
import java.util.UUID

@Service
class RefreshTokenService(
    private val refreshTokenRepository: RefreshTokenRepository,
    private val jwtProperties: JwtProperties,
) {

    fun create(user: UserEntity): String {
        val rawToken = UUID.randomUUID().toString() + UUID.randomUUID()
        val token = RefreshTokenEntity(
            user = user,
            tokenHash = hash(rawToken),
            expiresAt = Instant.now().plus(jwtProperties.refreshTokenTtl),
        )
        refreshTokenRepository.save(token)
        return rawToken
    }

    fun consume(rawToken: String): UserEntity {
        val token = refreshTokenRepository.findByTokenHash(hash(rawToken))
            .orElseThrow { IllegalArgumentException("Invalid refresh token") }
        if (!token.isActive()) {
            throw IllegalArgumentException("Invalid refresh token")
        }
        token.revoke()
        refreshTokenRepository.save(token)
        return token.user
    }

    fun revoke(rawToken: String) {
        refreshTokenRepository.findByTokenHash(hash(rawToken)).ifPresent { token ->
            token.revoke()
            refreshTokenRepository.save(token)
        }
    }

    private fun hash(value: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(value.toByteArray(StandardCharsets.UTF_8))
        return HexFormat.of().formatHex(digest)
    }
}
