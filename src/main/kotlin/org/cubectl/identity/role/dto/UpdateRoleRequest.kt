package org.cubectl.identity.role.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class UpdateRoleRequest(
    @field:NotBlank
    @field:Size(max = 128)
    val displayName: String,
    val description: String?,
    @field:NotNull
    val permissionCodes: Set<String>,
)
