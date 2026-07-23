package secureauth.security;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import secureauth.config.DatabaseConnection;
import secureauth.model.User;

/**
 * Gestiona la sesión del usuario actual, el caché de módulos de negocio y el control de accesos.
 */
public final class SessionManager {

    private static final Logger LOGGER = Logger.getLogger(SessionManager.class.getName());
    private static final SessionManager INSTANCE = new SessionManager();

    private User currentUser;
    private final Set<String> userPermissions = new HashSet<>();
    private final Set<String> enabledModules = new HashSet<>();

    private SessionManager() {}

    public static SessionManager getInstance() {
        return INSTANCE;
    }

    public synchronized User getCurrentUser() {
        return currentUser;
    }

    /**
     * Inicializa la sesión del usuario actual, cargando sus permisos reales y cacheando los módulos habilitados.
     */
    public synchronized void initializeSession(User user) {
        this.currentUser = user;
        this.userPermissions.clear();
        this.enabledModules.clear();

        if (user == null) {
            return;
        }

        // 1. Cargar permisos reales desde base de datos
        loadPermissionsFromDatabase(user);

        // 2. Si no hay permisos definidos en BD, aplicar fallback dinámico de roles para compatibilidad legacy
        if (userPermissions.isEmpty()) {
            applyFallbackPermissions(user.getRolId());
        }

        // 3. Cargar y cachear los módulos de negocio según el tipo de negocio activo
        cacheBusinessModules();
    }

    public synchronized void closeSession() {
        this.currentUser = null;
        this.userPermissions.clear();
        this.enabledModules.clear();
    }

    public synchronized boolean hasPermission(String permissionName) {
        if (currentUser == null) {
            return false;
        }
        // Admin gets absolute override
        if (currentUser.getRolId() == 1) {
            return true;
        }
        return userPermissions.contains(permissionName);
    }

    public synchronized boolean isModuleEnabled(String moduleName) {
        if (enabledModules.isEmpty()) {
            return true; // Default to true if not yet initialized or no business defined
        }
        return enabledModules.contains(moduleName.toUpperCase().trim());
    }

    private void loadPermissionsFromDatabase(User user) {
        String sql = """
            SELECT p.name FROM role_permissions rp
            JOIN permissions p ON p.id = rp.permission_id
            WHERE rp.role_id = ?
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, user.getRolId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    userPermissions.add(rs.getString("name"));
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error al cargar permisos del usuario de la BD. Se usará fallback.", e);
        }
    }

    private void applyFallbackPermissions(int roleId) {
        LOGGER.info("Aplicando mapeo fallback dinámico de permisos para el rol ID: " + roleId);
        // Fallback mapping matching database seed
        userPermissions.add("MODULO_DASHBOARD");
        userPermissions.add("MODULO_VENTAS");

        if (roleId == 1) { // Administrador
            userPermissions.add("MODULO_INVENTARIO");
            userPermissions.add("MODULO_REPORTES");
            userPermissions.add("MODULO_CONFIGURACION");
            userPermissions.add("MODULO_USUARIOS");
            userPermissions.add("ACCION_CREAR");
            userPermissions.add("ACCION_EDITAR");
            userPermissions.add("ACCION_ELIMINAR");
            userPermissions.add("ACCION_EXPORTAR");
            userPermissions.add("ACCION_CONFIGURACION_CRITICA");
        } else if (roleId == 2) { // Supervisor
            userPermissions.add("MODULO_INVENTARIO");
            userPermissions.add("MODULO_REPORTES");
            userPermissions.add("ACCION_CREAR");
            userPermissions.add("ACCION_EDITAR");
            userPermissions.add("ACCION_ELIMINAR");
            userPermissions.add("ACCION_EXPORTAR");
        } else if (roleId == 3 || roleId == 4) { // Recepcionista o Médico
            userPermissions.add("ACCION_CREAR");
            userPermissions.add("ACCION_EDITAR");
            if (roleId == 4) {
                userPermissions.add("MODULO_INVENTARIO");
            }
        }
    }

    private void cacheBusinessModules() {
        // Encontrar tipo de negocio activo
        String businessTypeName = "Veterinaria"; // Valor de fallback por defecto
        String sql = """
            SELECT bt.name FROM business b
            JOIN business_type bt ON bt.id = b.business_type_id
            LIMIT 1
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                businessTypeName = rs.getString("name");
            }
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "No se pudo obtener el rubro del negocio de la BD. Usando Veterinaria.", e);
        }

        LOGGER.info("Cacheando módulos habilitados para el rubro de negocio: " + businessTypeName);

        // Activación de módulos por rubro
        enabledModules.add("DASHBOARD");
        enabledModules.add("VENTAS");
        enabledModules.add("REPORTES");

        if (businessTypeName.equalsIgnoreCase("Veterinaria") || businessTypeName.equalsIgnoreCase("Clínica")) {
            enabledModules.add("CITAS");
            enabledModules.add("SERVICIOS");
            enabledModules.add("INVENTARIO");
            enabledModules.add("CONFIGURACION");
            enabledModules.add("USUARIOS");
        } else if (businessTypeName.equalsIgnoreCase("Peluquería") || businessTypeName.equalsIgnoreCase("Baños")) {
            enabledModules.add("CITAS");
            enabledModules.add("SERVICIOS");
            enabledModules.add("CONFIGURACION");
        } else if (businessTypeName.equalsIgnoreCase("Tienda")) {
            enabledModules.add("INVENTARIO");
            enabledModules.add("CONFIGURACION");
        } else {
            // General multi-negocio fallback
            enabledModules.add("CITAS");
            enabledModules.add("INVENTARIO");
            enabledModules.add("CONFIGURACION");
            enabledModules.add("USUARIOS");
        }
    }
}
