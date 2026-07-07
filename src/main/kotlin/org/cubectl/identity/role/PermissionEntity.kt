package org.cubectl.identity.role

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.PrePersist
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "permissions")
class PermissionEntity(
    @Column(nullable = false, unique = true)
    var code: String,
    var description: String?,
) {
    @Id
    var id: UUID = UUID.randomUUID()

    @Column(name = "created_at", nullable = false)
    lateinit var createdAt: Instant

    @PrePersist
    fun prePersist() {
        createdAt = Instant.now()
    }
}
