package org.cubectl.identity.user;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PERMISSION_user_read') or hasAuthority('PERMISSION_user_manage')")
    public List<UserResponse> users() {
        return userRepository.findAll().stream()
                .map(UserResponse::from)
                .toList();
    }

    @GetMapping("/{user_id}")
    @PreAuthorize("hasAuthority('PERMISSION_user_read') or hasAuthority('PERMISSION_user_manage')")
    public UserResponse user(@PathVariable("user_id") UUID userId) {
        return userRepository.findById(userId)
                .map(UserResponse::from)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    @PatchMapping("/{user_id}")
    @PreAuthorize("hasAuthority('PERMISSION_user_manage')")
    public UserResponse update(@PathVariable("user_id") UUID userId, @Valid @RequestBody UpdateUserRequest request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (request.username() != null) {
            user.setUsername(request.username());
        }
        if (request.status() != null) {
            user.setStatus(request.status());
        }
        return UserResponse.from(userRepository.save(user));
    }

    @DeleteMapping("/{user_id}")
    @PreAuthorize("hasAuthority('PERMISSION_user_manage')")
    public ResponseEntity<Void> delete(@PathVariable("user_id") UUID userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setStatus(UserStatus.DELETED);
        userRepository.save(user);
        return ResponseEntity.noContent().build();
    }

    public record UpdateUserRequest(@Size(min = 3, max = 64) String username, UserStatus status) {
    }

    public record UserResponse(UUID id, String username, UserStatus status) {
        static UserResponse from(UserEntity user) {
            return new UserResponse(user.getId(), user.getUsername(), user.getStatus());
        }
    }
}
