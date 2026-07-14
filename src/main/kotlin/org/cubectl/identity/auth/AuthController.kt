package org.cubectl.identity.auth

import jakarta.validation.Valid
import org.cubectl.identity.auth.dto.AuthRequest
import org.cubectl.identity.auth.dto.AuthResponse
import org.cubectl.identity.auth.dto.LogoutRequest
import org.cubectl.identity.auth.dto.MeResponse
import org.cubectl.identity.auth.dto.RefreshRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

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
}
