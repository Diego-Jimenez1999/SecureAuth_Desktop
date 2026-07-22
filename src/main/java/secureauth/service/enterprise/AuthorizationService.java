package secureauth.service.enterprise;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import secureauth.config.DatabaseConnection;
import secureauth.shared.error.AccessDeniedException;

/**
 * Servicio encargado de validar todas las operaciones y privilegios del sistema.
 *
 * <p>Asegura el cumplimiento de control de acceso estricto basado en roles (RBAC)
 * validando que las peticiones a nivel de Servicio, Casos de Uso y Controladores
 * cuenten con los permisos indicados en la sesión activa. Registra automáticamente
 * cada intento autorizado y denegado en el historial de seguridad.</p>
 *
 * @author Jules
 * @version 1.0
 */
public class AuthorizationService {

    private static final Logger LOGGER = Logger.getLogger(AuthorizationService.class.getName());

    /**
     * Valida de forma estricta si el usuario de la sesión actual cuenta con el permiso
     * especificado para realizar una acción en un módulo determinado.
     *
     * <p>Si el usuario no cuenta con el permiso o no ha iniciado sesión, registra el
     * intento de acceso no autorizado, cancela de inmediato la operación lanzando una
     * excepción {@link AccessDeniedException} de negocio, e impide la ejecución.</p>
     *
     * @param permissionId identificador único del permiso requerido
     * @param moduleName nombre del módulo sobre el cual se realiza la operación
     * @param actionName descripción legible de la acción que se intenta ejecutar
     * @throws AccessDeniedException si el acceso es denegado o no hay sesión activa
     */
    public void checkPermission(int permissionId, String moduleName, String actionName) {
        SessionManager session = SessionManager.getInstance();
        if (session.getCurrentUser() == null) {
            logSecurityEvent(0, "Invitado", 0, 0, moduleName, actionName, "DENIED", "Intento de acceso sin iniciar sesión.");
            throw new AccessDeniedException("No hay ninguna sesión activa en el sistema.");
        }

        int userId = session.getCurrentUser().getId();
        int roleId = session.getCurrentUser().getRolId();
        int businessId = session.getActiveBusiness() != null ? session.getActiveBusiness().id() : 0;
        int branchId = session.getActiveBranch() != null ? session.getActiveBranch().id() : 0;
        String userEmail = session.getCurrentUser().getEmail();

        // El Administrador (ID de rol 1) cuenta con acceso total absoluto a todo el sistema
        if (roleId == 1) {
            logSecurityEvent(userId, userEmail, businessId, branchId, moduleName, actionName, "ALLOWED", "Acceso total concedido al Administrador.");
            return;
        }

        // Validación basada en la relación en memoria cargada desde roles y permisos
        if (!session.hasPermission(permissionId)) {
            String desc = "Intento de ejecutar la acción '" + actionName + "' en el módulo '" + moduleName + "' sin el permiso ID " + permissionId;
            logSecurityEvent(userId, userEmail, businessId, branchId, moduleName, actionName, "DENIED", desc);
            throw new AccessDeniedException("Acceso denegado: No cuenta con los privilegios de rol necesarios para esta operación.");
        }

        // Acceso concedido exitosamente
        logSecurityEvent(userId, userEmail, businessId, branchId, moduleName, actionName, "ALLOWED", "Acceso autorizado.");
    }

    /**
     * Registra de forma asincrónica o directa un evento de seguridad de auditoría
     * en la tabla {@code security_activity_log} de la base de datos MySQL.
     *
     * @param userId ID del usuario responsable (o 0 si no se encuentra autenticado)
     * @param userEmail correo electrónico de la cuenta (o "Invitado")
     * @param businessId ID del negocio activo (o 0)
     * @param branchId ID de la sucursal activa (o 0)
     * @param moduleName nombre del módulo involucrado
     * @param actionName nombre de la acción realizada
     * @param status resultado del evento (ej. 'SUCCESS', 'FAILED', 'ALLOWED', 'DENIED')
     * @param description descripción detallada del suceso
     */
    public void logSecurityEvent(int userId, String userEmail, int businessId, int branchId,
                                 String moduleName, String actionName, String status, String description) {
        String sql = """
            INSERT INTO security_activity_log (user_id, business_id, branch_id, module_name, action_name, status, description)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        LOGGER.info(String.format("SEGURIDAD [%s] - Usuario: %s | Módulo: %s | Acción: %s | Detalles: %s",
                status, userEmail, moduleName, actionName, description));

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (userId > 0) {
                ps.setInt(1, userId);
            } else {
                ps.setNull(1, java.sql.Types.INTEGER);
            }

            if (businessId > 0) {
                ps.setInt(2, businessId);
            } else {
                ps.setNull(2, java.sql.Types.INTEGER);
            }

            if (branchId > 0) {
                ps.setInt(3, branchId);
            } else {
                ps.setNull(3, java.sql.Types.INTEGER);
            }

            ps.setString(4, moduleName);
            ps.setString(5, actionName);
            ps.setString(6, status);
            ps.setString(7, description);

            ps.executeUpdate();

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error grave al registrar auditoría en security_activity_log", e);
        }
    }
}
