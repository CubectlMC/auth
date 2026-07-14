package org.cubectl.identity.auth.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class AuthRequest(
    @field:NotBlank
    @field:Size(min = 3, max = 64)
    val username: String,
    @field:NotBlank
    @field:Size(min = 8, max = 128)
    val password: String,
)
