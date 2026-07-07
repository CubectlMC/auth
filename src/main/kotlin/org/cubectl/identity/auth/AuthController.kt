package org.cubectl.identity.auth

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService,
) {

    @PostMapping("/register")
    fun register(@Valid @RequestBody request: AuthRequest): ResponseEntity<AuthResponse> {
        val token = authService.register(request.username, request.password)
        return ResponseEntity.status(HttpStatus.CREATED).body(AuthResponse.from(token))
    }

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: AuthRequest): ResponseEntity<AuthResponse> =
        ResponseEntity.ok(AuthResponse.from(authService.login(request.username, request.password)))

    @PostMapping("/logout")
    fun logout(@RequestBody(required = false) request: LogoutRequest?): ResponseEntity<Void> {
        authService.logout(request?.refreshToken)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/refresh")
    fun refresh(@Valid @RequestBody request: RefreshRequest): ResponseEntity<AuthResponse> =
        ResponseEntity.ok(AuthResponse.from(authService.refresh(request.refreshToken)))

    @GetMapping("/me")
    fun me(@AuthenticationPrincipal user: AuthenticatedUser): ResponseEntity<MeResponse> =
        ResponseEntity.ok(MeResponse(user.userId, user.username, user.permissions))

    data class AuthRequest(
        @field:NotBlank
        @field:Size(min = 3, max = 64)
        val username: String,
        @field:NotBlank
        @field:Size(min = 8, max = 128)
        val password: String,
    )

    data class RefreshRequest(
        @field:NotBlank
        val refreshToken: String,
    )

    data class LogoutRequest(
        val refreshToken: String?,
    )

    data class AuthResponse(
        val accessToken: String,
        val refreshToken: String,
        val tokenType: String,
    ) {
        companion object {
            fun from(token: AuthService.TokenResponse): AuthResponse =
                AuthResponse(token.accessToken, token.refreshToken, token.tokenType)
        }
    }

    data class MeResponse(
        val userId: UUID,
        val username: String,
        val permissions: Set<String>,
    )
}
