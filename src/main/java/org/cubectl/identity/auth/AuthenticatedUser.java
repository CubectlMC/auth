package org.cubectl.identity.auth;

import java.util.Set;
import java.util.UUID;

public record AuthenticatedUser(UUID userId, String username, Set<String> permissions) {
}
