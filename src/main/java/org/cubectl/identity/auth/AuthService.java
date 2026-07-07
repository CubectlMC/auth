package org.cubectl.identity.auth;

import java.util.Set;
import java.util.stream.Collectors;

import org.cubectl.identity.role.PermissionEntity;
import org.cubectl.identity.role.RoleEntity;
import org.cubectl.identity.role.RoleRepository;
import org.cubectl.identity.user.UserEntity;
import org.cubectl.identity.user.UserRepository;
import org.cubectl.identity.user.UserStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            RefreshTokenService refreshTokenService
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional
    public TokenResponse register(String username, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }
        boolean firstUser = userRepository.count() == 0;
        UserEntity user = userRepository.save(new UserEntity(username, passwordEncoder.encode(password)));
        roleRepository.findByCode(firstUser ? "admin" : "viewer").ifPresent(role -> user.getRoles().add(role));
        return issueTokens(user);
    }

    @Transactional
    public TokenResponse login(String username, String password) {
        UserEntity user = userRepository.findWithRolesByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        if (user.getStatus() != UserStatus.ACTIVE || !passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        user.markLogin();
        return issueTokens(user);
    }

    @Transactional
    public TokenResponse refresh(String refreshToken) {
        UserEntity user = refreshTokenService.consume(refreshToken);
        return issueTokens(userRepository.findWithRolesById(user.getId()).orElseThrow());
    }

    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            refreshTokenService.revoke(refreshToken);
        }
    }

    private TokenResponse issueTokens(UserEntity user) {
        Set<String> permissions = permissions(user);
        return new TokenResponse(
                jwtService.issueAccessToken(user.getId(), user.getUsername(), permissions),
                refreshTokenService.create(user),
                "Bearer"
        );
    }

    private Set<String> permissions(UserEntity user) {
        return user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(PermissionEntity::getCode)
                .collect(Collectors.toUnmodifiableSet());
    }

    public record TokenResponse(String accessToken, String refreshToken, String tokenType) {
    }
}
