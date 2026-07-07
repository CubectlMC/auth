package org.cubectl.identity.role;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/roles")
public class RoleController {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public RoleController(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PERMISSION_role_read') or hasAuthority('PERMISSION_role_manage')")
    public List<RoleResponse> roles() {
        return roleRepository.findAll().stream()
                .map(RoleResponse::from)
                .toList();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PERMISSION_role_manage')")
    public ResponseEntity<RoleResponse> create(@Valid @RequestBody CreateRoleRequest request) {
        if (roleRepository.existsByCode(request.code())) {
            throw new IllegalArgumentException("Role already exists");
        }
        RoleEntity role = new RoleEntity(request.code(), request.displayName(), request.description(), false);
        role.getPermissions().addAll(resolvePermissions(request.permissionCodes()));
        return ResponseEntity.status(HttpStatus.CREATED).body(RoleResponse.from(roleRepository.save(role)));
    }

    @GetMapping("/{role_id}")
    @PreAuthorize("hasAuthority('PERMISSION_role_read') or hasAuthority('PERMISSION_role_manage')")
    public RoleResponse role(@PathVariable("role_id") UUID roleId) {
        return roleRepository.findWithPermissionsById(roleId)
                .map(RoleResponse::from)
                .orElseThrow(() -> new IllegalArgumentException("Role not found"));
    }

    @PatchMapping("/{role_id}")
    @PreAuthorize("hasAuthority('PERMISSION_role_manage')")
    public RoleResponse update(@PathVariable("role_id") UUID roleId, @Valid @RequestBody UpdateRoleRequest request) {
        RoleEntity role = roleRepository.findWithPermissionsById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found"));
        role.update(request.displayName(), request.description(), resolvePermissions(request.permissionCodes()));
        return RoleResponse.from(roleRepository.save(role));
    }

    @DeleteMapping("/{role_id}")
    @PreAuthorize("hasAuthority('PERMISSION_role_manage')")
    public ResponseEntity<Void> delete(@PathVariable("role_id") UUID roleId) {
        RoleEntity role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found"));
        if (role.isSystemRole()) {
            throw new IllegalArgumentException("System role cannot be deleted");
        }
        roleRepository.delete(role);
        return ResponseEntity.noContent().build();
    }

    private Set<PermissionEntity> resolvePermissions(Set<String> codes) {
        List<PermissionEntity> permissions = permissionRepository.findByCodeIn(codes);
        if (permissions.size() != codes.size()) {
            throw new IllegalArgumentException("Unknown permission code");
        }
        return new HashSet<>(permissions);
    }

    public record CreateRoleRequest(
            @NotBlank @Size(max = 64) String code,
            @NotBlank @Size(max = 128) String displayName,
            String description,
            @NotNull Set<String> permissionCodes
    ) {
    }

    public record UpdateRoleRequest(
            @NotBlank @Size(max = 128) String displayName,
            String description,
            @NotNull Set<String> permissionCodes
    ) {
    }

    public record RoleResponse(UUID id, String code, String displayName, String description, Set<String> permissionCodes) {
        public static RoleResponse from(RoleEntity role) {
            return new RoleResponse(
                    role.getId(),
                    role.getCode(),
                    role.getDisplayName(),
                    role.getDescription(),
                    role.getPermissions().stream().map(PermissionEntity::getCode).collect(java.util.stream.Collectors.toSet())
            );
        }
    }
}
