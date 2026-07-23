package secureauth.security;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import secureauth.model.User;

class SessionManagerTest {

    @Test
    void testSessionManagerInitializationAndFallbackPermissions() {
        SessionManager sessionManager = SessionManager.getInstance();
        assertNotNull(sessionManager);

        User testUser = new User();
        testUser.setId(99);
        testUser.setEmail("recepcionista@secureauth.com");
        testUser.setRolId(3); // Recepcionista

        sessionManager.initializeSession(testUser);
        assertSame(testUser, sessionManager.getCurrentUser());

        // Recepcionista gets MODULO_VENTAS and ACCION_CREAR fallback
        assertTrue(sessionManager.hasPermission("MODULO_VENTAS"));
        assertTrue(sessionManager.hasPermission("ACCION_CREAR"));
        assertFalse(sessionManager.hasPermission("ACCION_CONFIGURACION_CRITICA"));

        // Close session
        sessionManager.closeSession();
        assertNull(sessionManager.getCurrentUser());
    }

    @Test
    void testAdminPermissionOverride() {
        SessionManager sessionManager = SessionManager.getInstance();
        User adminUser = new User();
        adminUser.setId(1);
        adminUser.setEmail("admin@secureauth.com");
        adminUser.setRolId(1); // Administrador

        sessionManager.initializeSession(adminUser);
        assertTrue(sessionManager.hasPermission("ACCION_CONFIGURACION_CRITICA"));
        assertTrue(sessionManager.hasPermission("ANY_OTHER_RANDOM_PERMISSION"));

        sessionManager.closeSession();
    }
}
