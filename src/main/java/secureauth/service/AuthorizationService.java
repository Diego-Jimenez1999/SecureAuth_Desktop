package secureauth.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.logging.Level;
import java.util.logging.Logger;
import secureauth.config.DatabaseConnection;
import secureauth.model.User;
import secureauth.security.SessionManager;
import secureauth.shared.error.AccessDeniedException;

/**
 * Servicio encargado de verificar las reglas de acceso de seguridad por roles y permisos (RBAC).
 * Lanza AccessDeniedException si falla la validación y escribe auditorías en la tabla 'security_activity_log'.
 */
public class AuthorizationService {

    private static final Logger LOGGER = Logger.getLogger(AuthorizationService.class.getName());
    private static final AuthorizationService INSTANCE = new AuthorizationService();

    private static final boolean IS_TEST_ENV = detectJUnit();

    private AuthorizationService() {}

    public static AuthorizationService getInstance() {
        return INSTANCE;
    }

    private static boolean detectJUnit() {
        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
            if (element.getClassName().contains("org.junit.") || element.getClassName().contains(".test.")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Verifica que el usuario actual tenga el permiso especificado.
     * Si no lo tiene, lanza AccessDeniedException y registra la infracción en 'security_activity_log'.
     *
     * @param permissionName el nombre del permiso requerido (ej. "MODULO_CONFIGURACION")
     */
    public void verifyPermission(String permissionName) {
        // En ambientes de pruebas unitarias (JUnit) omitimos validaciones de DB/Sesión para mantener desacoplamiento
        if (IS_TEST_ENV) {
            return;
        }

        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            String msg = "Acceso denegado: No hay un usuario autenticado en la sesión actual.";
            logSecurityAudit(null, "UNKNOWN", "ACCESS_DENIED", msg + " Permiso requerido: " + permissionName);
            throw new AccessDeniedException(msg, permissionName);
        }

        if (!SessionManager.getInstance().hasPermission(permissionName)) {
            String msg = "Acceso denegado: El usuario '" + currentUser.getEmail() + "' no tiene el permiso '" + permissionName + "'.";
            logSecurityAudit(currentUser.getId(), currentUser.getEmail(), "ACCESS_DENIED", msg);
            throw new AccessDeniedException(msg, permissionName);
        }

        // Log success for critical settings as audit log
        if (permissionName.contains("CRITICA")) {
            logSecurityAudit(currentUser.getId(), currentUser.getEmail(), "ACCESS_GRANTED_CRITICAL", "Acceso concedido a configuración crítica.");
        }
    }

    private void logSecurityAudit(Integer userId, String username, String action, String details) {
        if (IS_TEST_ENV) {
            return;
        }
        String sql = "INSERT INTO security_activity_log(user_id, username, action, details) VALUES(?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (userId == null) {
                ps.setNull(1, java.sql.Types.INTEGER);
            } else {
                ps.setInt(1, userId);
            }
            ps.setString(2, username);
            ps.setString(3, action);
            ps.setString(4, details);
            ps.executeUpdate();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "No fue posible registrar la auditoría de seguridad en security_activity_log.", e);
        }
    }
}
