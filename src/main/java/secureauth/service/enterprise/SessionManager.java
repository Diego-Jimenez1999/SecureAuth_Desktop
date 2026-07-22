package secureauth.service.enterprise;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import secureauth.config.DatabaseConnection;
import secureauth.model.User;
import secureauth.model.enterprise.Business;
import secureauth.model.enterprise.Branch;

/**
 * Gestor único en memoria de la sesión activa del sistema (SessionManager).
 *
 * <p>Almacena la información de seguridad básica para evitar consultas reiteradas
 * a la base de datos MySQL durante la navegación del usuario. Centraliza:
 * <ul>
 *   <li>El usuario actualmente autenticado</li>
 *   <li>La empresa o negocio activo</li>
 *   <li>La sucursal activa</li>
 *   <li>El rol del usuario autenticado</li>
 *   <li>La lista de permisos asignados al rol</li>
 *   <li>Los parámetros de configuración operativos del sistema</li>
 * </ul>
 * </p>
 *
 * @author Jules
 * @version 1.0
 */
public final class SessionManager {

    private static final Logger LOGGER = Logger.getLogger(SessionManager.class.getName());

    /** Instancia única bajo el patrón Singleton. */
    private static final SessionManager INSTANCE = new SessionManager();

    private User currentUser;
    private Business activeBusiness;
    private Branch activeBranch;
    private String roleName;
    private final List<Integer> permissionIds = new ArrayList<>();
    private final List<String> permissionNames = new ArrayList<>();
    private final Map<String, String> systemSettings = new HashMap<>();

    /**
     * Constructor privado para restringir instanciación externa.
     */
    private SessionManager() {}

    /**
     * Retorna la instancia Singleton única del SessionManager.
     *
     * @return instancia de SessionManager
     */
    public static SessionManager getInstance() {
        return INSTANCE;
    }

    /**
     * Inicializa por completo la sesión del usuario cargando toda la información
     * asociada de forma atómica y en una única operación tras el inicio de sesión exitoso.
     *
     * @param user el usuario autenticado que inicia sesión
     */
    public synchronized void initializeSession(User user) {
        this.currentUser = user;
        this.permissionIds.clear();
        this.permissionNames.clear();
        this.systemSettings.clear();

        if (user == null) {
            this.activeBusiness = null;
            this.activeBranch = null;
            this.roleName = null;
            return;
        }

        LOGGER.info("Inicializando sesión en memoria para usuario: " + user.getEmail());

        try (Connection conn = DatabaseConnection.getConnection()) {
            // 1. Obtener el nombre del rol asignado al usuario
            String roleSql = "SELECT nombre_rol FROM roles WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(roleSql)) {
                ps.setInt(1, user.getRolId());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        this.roleName = rs.getString("nombre_rol");
                    } else {
                        this.roleName = "Invitado";
                    }
                }
            }

            // 2. Cargar los permisos asociados al rol mediante las relaciones
            // roles -> role_permissions -> permissions
            String permSql = """
                SELECT p.id, p.name
                FROM role_permissions rp
                JOIN permissions p ON rp.permission_id = p.id
                WHERE rp.role_id = ?
                """;
            try (PreparedStatement ps = conn.prepareStatement(permSql)) {
                ps.setInt(1, user.getRolId());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        this.permissionIds.add(rs.getInt("id"));
                        this.permissionNames.add(rs.getString("name"));
                    }
                }
            }

            // 3. Obtener empresa y sucursal activas desde el contexto de negocio
            int activeBusId = EnterpriseContext.getInstance().getActiveBusinessId();
            int activeBrId = EnterpriseContext.getInstance().getActiveBranchId();

            String busSql = "SELECT id, business_type_id, name, nit, address, phone, logo, primary_color, secondary_color FROM business WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(busSql)) {
                ps.setInt(1, activeBusId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        this.activeBusiness = new Business(
                            rs.getInt("id"),
                            rs.getInt("business_type_id"),
                            rs.getString("name"),
                            rs.getString("nit"),
                            rs.getString("address"),
                            rs.getString("phone"),
                            rs.getString("logo"),
                            rs.getString("primary_color"),
                            rs.getString("secondary_color")
                        );
                    }
                }
            }

            String brSql = "SELECT id, business_id, branch_name, address, phone, status FROM branches WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(brSql)) {
                ps.setInt(1, activeBrId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        this.activeBranch = new Branch(
                            rs.getInt("id"),
                            rs.getInt("business_id"),
                            rs.getString("branch_name"),
                            rs.getString("address"),
                            rs.getString("phone"),
                            rs.getString("status")
                        );
                    }
                }
            }

            // 4. Cargar configuraciones operativas desde app_settings para la empresa activa
            String settingsSql = "SELECT clave, valor FROM app_settings WHERE business_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(settingsSql)) {
                ps.setInt(1, activeBusId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        this.systemSettings.put(rs.getString("clave"), rs.getString("valor"));
                    }
                }
            }

            LOGGER.info("Sesión inicializada exitosamente. Rol: " + roleName + ", Permisos: " + permissionIds.size());

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Fallo grave al inicializar la sesión en el SessionManager", e);
        }
    }

    /**
     * Retorna el usuario actualmente autenticado.
     *
     * @return usuario de sesión
     */
    public synchronized User getCurrentUser() {
        return currentUser;
    }

    /**
     * Retorna la empresa o negocio activo en el sistema.
     *
     * @return empresa activa
     */
    public synchronized Business getActiveBusiness() {
        return activeBusiness;
    }

    /**
     * Retorna la sucursal activa en el sistema.
     *
     * @return sucursal activa
     */
    public synchronized Branch getActiveBranch() {
        return activeBranch;
    }

    /**
     * Retorna el nombre legible del rol del usuario.
     *
     * @return nombre del rol
     */
    public synchronized String getRoleName() {
        return roleName;
    }

    /**
     * Retorna una copia de la lista de identificadores de permisos cargados.
     *
     * @return lista de IDs de permisos
     */
    public synchronized List<Integer> getPermissionIds() {
        return new ArrayList<>(permissionIds);
    }

    /**
     * Retorna una copia de la lista de nombres de permisos cargados.
     *
     * @return lista de nombres de permisos
     */
    public synchronized List<String> getPermissionNames() {
        return new ArrayList<>(permissionNames);
    }

    /**
     * Retorna una configuración de la aplicación por su clave.
     *
     * @param clave identificador de la configuración
     * @return valor asociado, o {@code null} si no existe
     */
    public synchronized String getSetting(String clave) {
        return systemSettings.get(clave);
    }

    /**
     * Establece o actualiza de forma temporal una configuración del sistema en memoria.
     *
     * @param clave identificador de la configuración
     * @param valor valor asociado
     */
    public synchronized void setSetting(String clave, String valor) {
        systemSettings.put(clave, valor);
    }

    /**
     * Comprueba si el usuario autenticado cuenta con un permiso específico cargado en memoria.
     *
     * @param permissionId identificador del permiso
     * @return {@code true} si cuenta con el permiso, de lo contrario {@code false}
     */
    public synchronized boolean hasPermission(int permissionId) {
        return permissionIds.contains(permissionId);
    }

    /**
     * Libera de forma completa el estado en memoria al cerrar sesión.
     */
    public synchronized void clearSession() {
        this.currentUser = null;
        this.activeBusiness = null;
        this.activeBranch = null;
        this.roleName = null;
        this.permissionIds.clear();
        this.permissionNames.clear();
        this.systemSettings.clear();
        LOGGER.info("Estado de sesión limpiado del SessionManager.");
    }
}
