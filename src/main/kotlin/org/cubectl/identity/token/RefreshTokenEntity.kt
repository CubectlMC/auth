package org.cubectl.identity.token

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.PrePersist
import jakarta.persistence.Table
import org.cubectl.identity.user.UserEntity
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "refresh_tokens")
class RefreshTokenEntity(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    var user: UserEntity,
    @Column(name = "token_hash", nullable = false, unique = true)
    var tokenHash: String,
    @Column(name = "expires_at", nullable = false)
    var expiresAt: Instant,
) {
    @Id
    var id: UUID = UUID.randomUUID()

    @Column(name = "revoked_at")
    var revokedAt: Instant? = null

    @Column(name = "created_at", nullable = false)
    lateinit var createdAt: Instant

    @PrePersist
    fun prePersist() {
        createdAt = Instant.now()
    }

    fun isActive(): Boolean = revokedAt == null && expiresAt.isAfter(Instant.now())

    fun revoke() {
        revokedAt = Instant.now()
    }
}
