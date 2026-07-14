package org.cubectl.identity.auth

import org.cubectl.identity.auth.dto.TokenResponse
import org.cubectl.identity.role.PermissionEntity
import org.cubectl.identity.role.RoleRepository
import org.cubectl.identity.user.UserEntity
import org.cubectl.identity.user.UserRepository
import org.cubectl.identity.user.UserStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val refreshTokenService: RefreshTokenService,
) {

    @Transactional
    fun register(username: String, password: String): TokenResponse {
        if (userRepository.existsByUsername(username)) {
            throw IllegalArgumentException("Username already exists")
        }
        val firstUser = userRepository.count() == 0L
        val passwordHash = requireNotNull(passwordEncoder.encode(password)) { "Password encoder returned null" }
        val user = userRepository.save(UserEntity(username, passwordHash))
        roleRepository.findByCode(if (firstUser) "admin" else "viewer").ifPresent { role ->
            user.roles.add(role)
        }
        return issueTokens(user)
    }

    @Transactional
    fun login(username: String, password: String): TokenResponse {
        val user = userRepository.findWithRolesByUsername(username)
            .orElseThrow { IllegalArgumentException("Invalid credentials") }
        if (user.status != UserStatus.ACTIVE || !passwordEncoder.matches(password, user.passwordHash)) {
            throw IllegalArgumentException("Invalid credentials")
        }
        user.markLogin()
        return issueTokens(user)
    }

    @Transactional
    fun refresh(refreshToken: String): TokenResponse {
        val user = refreshTokenService.consume(refreshToken)
        return issueTokens(userRepository.findWithRolesById(user.id).orElseThrow())
    }

    @Transactional
    fun logout(refreshToken: String?) {
        if (!refreshToken.isNullOrBlank()) {
            refreshTokenService.revoke(refreshToken)
        }
    }

    private fun issueTokens(user: UserEntity): TokenResponse =
        TokenResponse(
            accessToken = jwtService.issueAccessToken(user.id, user.username, permissions(user)),
            refreshToken = refreshTokenService.create(user),
            tokenType = "Bearer",
        )

    private fun permissions(user: UserEntity): Set<String> =
        user.roles
            .flatMap { role -> role.permissions }
            .map(PermissionEntity::code)
            .toSet()
}
