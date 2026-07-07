package org.cubectl.identity.config;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.cubectl.identity.role.PermissionEntity;
import org.cubectl.identity.role.PermissionRepository;
import org.cubectl.identity.role.RoleEntity;
import org.cubectl.identity.role.RoleRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class IdentityDataSeeder implements ApplicationRunner {

    private static final Set<String> PERMISSIONS = Set.of(
            "user_read",
            "user_manage",
            "role_read",
            "role_manage",
            "permission_read",
            "instance_create",
            "instance_read",
            "instance_update",
            "instance_delete",
            "instance_start",
            "instance_stop",
            "instance_restart",
            "instance_runtime_update",
            "instance_java_version_update",
            "instance_member_manage",
            "instance_content_read",
            "instance_content_update",
            "instance_logs_read",
            "instance_live_logs_read",
            "resource_limits_read",
            "resource_limits_manage"
    );

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;

    public IdentityDataSeeder(PermissionRepository permissionRepository, RoleRepository roleRepository) {
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (String code : PERMISSIONS) {
            permissionRepository.findByCode(code)
                    .orElseGet(() -> permissionRepository.save(new PermissionEntity(code, code)));
        }

        Map<String, PermissionEntity> permissions = permissionRepository.findAll().stream()
                .collect(Collectors.toMap(PermissionEntity::getCode, permission -> permission));

        createRole("admin", "Admin", "Full access", permissions.keySet(), permissions);
        createRole("instance_creator", "Instance creator", "Can create and read instances", Set.of(
                "instance_create",
                "instance_read"
        ), permissions);
        createRole("viewer", "Viewer", "Read-only access", Set.of(
                "user_read",
                "role_read",
                "permission_read",
                "instance_read",
                "instance_content_read",
                "instance_logs_read",
                "instance_live_logs_read"
        ), permissions);
    }

    private void createRole(
            String code,
            String displayName,
            String description,
            Set<String> permissionCodes,
            Map<String, PermissionEntity> permissions
    ) {
        roleRepository.findByCode(code).orElseGet(() -> {
            RoleEntity role = new RoleEntity(code, displayName, description, true);
            role.getPermissions().addAll(permissionCodes.stream().map(permissions::get).toList());
            return roleRepository.save(role);
        });
    }
}
