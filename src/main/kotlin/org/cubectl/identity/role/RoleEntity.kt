package org.cubectl.identity.role

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "roles")
class RoleEntity(
    @Column(nullable = false, unique = true)
    var code: String,
    @Column(name = "display_name", nullable = false)
    var displayName: String,
    var description: String?,
    @Column(name = "system_role", nullable = false)
    var systemRole: Boolean,
) {
    @Id
    var id: UUID = UUID.randomUUID()

    @Column(name = "created_at", nullable = false)
    lateinit var createdAt: Instant

    @Column(name = "updated_at", nullable = false)
    lateinit var updatedAt: Instant

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "role_permissions",
        joinColumns = [JoinColumn(name = "role_id")],
        inverseJoinColumns = [JoinColumn(name = "permission_id")],
    )
    var permissions: MutableSet<PermissionEntity> = mutableSetOf()

    @PrePersist
    fun prePersist() {
        val now = Instant.now()
        createdAt = now
        updatedAt = now
    }

    @PreUpdate
    fun preUpdate() {
        updatedAt = Instant.now()
    }

    fun update(displayName: String, description: String?, permissions: Set<PermissionEntity>) {
        this.displayName = displayName
        this.description = description
        this.permissions = permissions.toMutableSet()
    }
}
