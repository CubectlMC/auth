package org.cubectl.identity.user;

import java.util.List;
import java.util.UUID;

import org.cubectl.identity.role.RoleEntity;
import org.cubectl.identity.role.RoleRepository;
import org.cubectl.identity.role.RoleController.RoleResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/{user_id}/roles")
public class UserRoleController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserRoleController(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PERMISSION_user_read') or hasAuthority('PERMISSION_user_manage')")
    public List<RoleResponse> roles(@PathVariable("user_id") UUID userId) {
        UserEntity user = userRepository.findWithRolesById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return user.getRoles().stream().map(RoleResponse::from).toList();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PERMISSION_user_manage')")
    public ResponseEntity<List<RoleResponse>> addRole(
            @PathVariable("user_id") UUID userId,
            @RequestBody AddUserRoleRequest request
    ) {
        UserEntity user = userRepository.findWithRolesById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        RoleEntity role = roleRepository.findWithPermissionsById(request.roleId())
                .orElseThrow(() -> new IllegalArgumentException("Role not found"));
        user.getRoles().add(role);
        userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(user.getRoles().stream().map(RoleResponse::from).toList());
    }

    @DeleteMapping("/{role_id}")
    @PreAuthorize("hasAuthority('PERMISSION_user_manage')")
    public ResponseEntity<Void> deleteRole(@PathVariable("user_id") UUID userId, @PathVariable("role_id") UUID roleId) {
        UserEntity user = userRepository.findWithRolesById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.getRoles().removeIf(role -> role.getId().equals(roleId));
        userRepository.save(user);
        return ResponseEntity.noContent().build();
    }

    public record AddUserRoleRequest(UUID roleId) {
    }
}
