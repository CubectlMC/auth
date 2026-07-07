package org.cubectl.identity.auth

import com.nimbusds.jose.JOSEException
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.MACSigner
import com.nimbusds.jose.crypto.MACVerifier
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.Date
import java.util.UUID
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

@Service
class JwtService(
    private val properties: JwtProperties,
) {
    private val secretKey: SecretKey = SecretKeySpec(
        properties.secret.toByteArray(StandardCharsets.UTF_8),
        "HmacSHA256",
    )

    fun issueAccessToken(userId: UUID, username: String, permissions: Set<String>): String {
        val now = Instant.now()
        val claims = JWTClaimsSet.Builder()
            .issuer(properties.issuer)
            .subject(userId.toString())
            .claim("username", username)
            .claim("permissions", permissions)
            .issueTime(Date.from(now))
            .expirationTime(Date.from(now.plus(properties.accessTokenTtl)))
            .build()

        val jwt = SignedJWT(JWSHeader(JWSAlgorithm.HS256), claims)
        try {
            jwt.sign(MACSigner(secretKey))
        } catch (ex: JOSEException) {
            throw IllegalStateException("Failed to sign JWT", ex)
        }
        return jwt.serialize()
    }

    fun parse(token: String): AuthenticatedUser {
        try {
            val jwt = SignedJWT.parse(token)
            if (!jwt.verify(MACVerifier(secretKey))) {
                throw IllegalArgumentException("Invalid token signature")
            }

            val claims = jwt.jwtClaimsSet
            if (claims.expirationTime.before(Date())) {
                throw IllegalArgumentException("Token expired")
            }

            val permissions = when (val claim = claims.getClaim("permissions")) {
                is Collection<*> -> claim.filterIsInstance<String>().toSet()
                else -> emptySet()
            }

            return AuthenticatedUser(
                userId = UUID.fromString(claims.subject),
                username = claims.getStringClaim("username"),
                permissions = permissions,
            )
        } catch (ex: Exception) {
            throw IllegalArgumentException("Invalid token", ex)
        }
    }
}
