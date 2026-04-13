package com.jarvis.deploy.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Controls read/write access to environment configs based on roles.
 * Supports per-environment and per-key permission grants.
 */
public class ConfigAccessController {

    public enum Permission { READ, WRITE }

    // role -> environment -> set of permissions
    private final Map<String, Map<String, Set<Permission>>> rolePermissions = new HashMap<>();

    /**
     * Grants a permission to a role for a specific environment.
     *
     * @param role       the role identifier
     * @param environment the environment name
     * @param permission  the permission to grant
     */
    public void grant(String role, String environment, Permission permission) {
        rolePermissions
            .computeIfAbsent(role, r -> new HashMap<>())
            .computeIfAbsent(environment, e -> new HashSet<>())
            .add(permission);
    }

    /**
     * Revokes a permission from a role for a specific environment.
     */
    public void revoke(String role, String environment, Permission permission) {
        Map<String, Set<Permission>> envMap = rolePermissions.get(role);
        if (envMap != null) {
            Set<Permission> perms = envMap.get(environment);
            if (perms != null) {
                perms.remove(permission);
            }
        }
    }

    /**
     * Checks whether the given role has the specified permission for an environment.
     *
     * @param role        the role identifier
     * @param environment the environment name
     * @param permission  the permission to check
     * @return true if the role has the permission, false otherwise
     */
    public boolean isAllowed(String role, String environment, Permission permission) {
        Map<String, Set<Permission>> envMap = rolePermissions.getOrDefault(role, Collections.emptyMap());
        Set<Permission> perms = envMap.getOrDefault(environment, Collections.emptySet());
        return perms.contains(permission);
    }

    /**
     * Returns all permissions a role has for a given environment.
     */
    public Set<Permission> getPermissions(String role, String environment) {
        return Collections.unmodifiableSet(
            rolePermissions
                .getOrDefault(role, Collections.emptyMap())
                .getOrDefault(environment, Collections.emptySet())
        );
    }

    /**
     * Enforces access, throwing AccessDeniedException if the role lacks the required permission.
     */
    public void enforce(String role, String environment, Permission permission) {
        if (!isAllowed(role, environment, permission)) {
            throw new AccessDeniedException(
                String.format("Role '%s' does not have %s access to environment '%s'",
                    role, permission, environment));
        }
    }
}
