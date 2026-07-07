package org.cubectl.identity.auth

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "cubectl.jwt")
data class JwtProperties(
    val issuer: String = "cubectl-auth",
    val secret: String = "dev-only-change-this-secret-dev-only-change-this-secret",
    val accessTokenTtl: Duration = Duration.ofMinutes(15),
    val refreshTokenTtl: Duration = Duration.ofDays(30),
)
