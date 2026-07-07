package org.cubectl.identity.common

import java.time.Instant

data class ApiError(
    val code: String,
    val message: String,
    val timestamp: Instant,
) {
    companion object {
        fun of(code: String, message: String): ApiError =
            ApiError(code = code, message = message, timestamp = Instant.now())
    }
}
