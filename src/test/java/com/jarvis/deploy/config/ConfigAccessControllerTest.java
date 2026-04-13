package com.jarvis.deploy.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ConfigAccessControllerTest {

    private ConfigAccessController controller;

    @BeforeEach
    void setUp() {
        controller = new ConfigAccessController();
    }

    @Test
    void grantedPermissionIsAllowed() {
        controller.grant("ops", "production", ConfigAccessController.Permission.READ);
        assertTrue(controller.isAllowed("ops", "production", ConfigAccessController.Permission.READ));
    }

    @Test
    void ungrantedPermissionIsNotAllowed() {
        assertFalse(controller.isAllowed("dev", "production", ConfigAccessController.Permission.WRITE));
    }

    @Test
    void revokedPermissionIsNoLongerAllowed() {
        controller.grant("ops", "staging", ConfigAccessController.Permission.WRITE);
        assertTrue(controller.isAllowed("ops", "staging", ConfigAccessController.Permission.WRITE));

        controller.revoke("ops", "staging", ConfigAccessController.Permission.WRITE);
        assertFalse(controller.isAllowed("ops", "staging", ConfigAccessController.Permission.WRITE));
    }

    @Test
    void multiplePermissionsCanBeGrantedToSameRole() {
        controller.grant("admin", "production", ConfigAccessController.Permission.READ);
        controller.grant("admin", "production", ConfigAccessController.Permission.WRITE);

        Set<ConfigAccessController.Permission> perms = controller.getPermissions("admin", "production");
        assertTrue(perms.contains(ConfigAccessController.Permission.READ));
        assertTrue(perms.contains(ConfigAccessController.Permission.WRITE));
    }

    @Test
    void permissionsAreEnvironmentScoped() {
        controller.grant("dev", "staging", ConfigAccessController.Permission.WRITE);
        assertFalse(controller.isAllowed("dev", "production", ConfigAccessController.Permission.WRITE));
        assertTrue(controller.isAllowed("dev", "staging", ConfigAccessController.Permission.WRITE));
    }

    @Test
    void enforceThrowsWhenPermissionMissing() {
        assertThrows(AccessDeniedException.class, () ->
            controller.enforce("guest", "production", ConfigAccessController.Permission.WRITE)
        );
    }

    @Test
    void enforcePassesWhenPermissionGranted() {
        controller.grant("ops", "production", ConfigAccessController.Permission.READ);
        assertDoesNotThrow(() ->
            controller.enforce("ops", "production", ConfigAccessController.Permission.READ)
        );
    }

    @Test
    void getPermissionsReturnsEmptySetForUnknownRole() {
        Set<ConfigAccessController.Permission> perms = controller.getPermissions("unknown", "production");
        assertTrue(perms.isEmpty());
    }

    @Test
    void revokeOnNonExistentRoleDoesNotThrow() {
        assertDoesNotThrow(() ->
            controller.revoke("ghost", "production", ConfigAccessController.Permission.READ)
        );
    }
}
