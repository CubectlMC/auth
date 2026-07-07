package org.cubectl.identity.role;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/permissions")
public class PermissionController {

    private final PermissionRepository permissionRepository;

    public PermissionController(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PERMISSION_permission_read')")
    public List<PermissionResponse> permissions() {
        return permissionRepository.findAll().stream()
                .map(permission -> new PermissionResponse(permission.getId(), permission.getCode(), permission.getDescription()))
                .toList();
    }

    public record PermissionResponse(UUID id, String code, String description) {
    }
}
