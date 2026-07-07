package org.cubectl.identity.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

import org.cubectl.identity.token.RefreshTokenEntity;
import org.cubectl.identity.token.RefreshTokenRepository;
import org.cubectl.identity.user.UserEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshTokenService {

    private final JwtProperties jwtProperties;
    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(JwtProperties jwtProperties, RefreshTokenRepository refreshTokenRepository) {
        this.jwtProperties = jwtProperties;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional
    public String create(UserEntity user) {
        String rawToken = UUID.randomUUID() + "." + UUID.randomUUID();
        RefreshTokenEntity entity = new RefreshTokenEntity(
                user,
                hash(rawToken),
                Instant.now().plus(jwtProperties.refreshTokenTtl())
        );
        refreshTokenRepository.save(entity);
        return rawToken;
    }

    @Transactional
    public UserEntity consume(String rawToken) {
        RefreshTokenEntity entity = refreshTokenRepository.findByTokenHash(hash(rawToken))
                .orElseThrow(() -> new IllegalArgumentException("Refresh token not found"));
        if (!entity.isActive()) {
            throw new IllegalArgumentException("Refresh token is not active");
        }
        entity.revoke();
        return entity.getUser();
    }

    @Transactional
    public void revoke(String rawToken) {
        refreshTokenRepository.findByTokenHash(hash(rawToken)).ifPresent(RefreshTokenEntity::revoke);
    }

    private String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }
}
