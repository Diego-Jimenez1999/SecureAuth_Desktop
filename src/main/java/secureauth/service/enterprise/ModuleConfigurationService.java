package secureauth.service.enterprise;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import secureauth.config.DatabaseConnection;

/**
 * Servicio encargado de gestionar la modularidad dinámica del sistema según el tipo de negocio.
 *
 * <p>Determina qué módulos específicos (Mascotas, Agenda, Ventas, Inventario, etc.)
 * se encuentran activos o inactivos consultando directamente las reglas establecidas
 * en la base de datos MySQL (tablas {@code business}, {@code business_type} y {@code business_modules}).
 * Evita el uso de condicionales estáticas o valores harcodeados en las interfaces de usuario.</p>
 *
 * @author Jules
 * @version 1.0
 */
public class ModuleConfigurationService {

    private static final Logger LOGGER = Logger.getLogger(ModuleConfigurationService.class.getName());

    /**
     * Consulta la base de datos para verificar si un módulo específico está activo o no
     * para el tipo de negocio indicado.
     *
     * @param businessTypeId identificador único del tipo de negocio
     * @param moduleName nombre comercial del módulo a consultar
     * @return {@code true} si el módulo se encuentra activo para ese rubro, o {@code false} en caso contrario
     */
    public boolean isModuleActive(int businessTypeId, String moduleName) {
        String sql = """
            SELECT active
            FROM business_modules
            WHERE business_type_id = ? AND LOWER(TRIM(module_name)) = LOWER(TRIM(?))
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, businessTypeId);
            ps.setString(2, moduleName);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("active");
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Fallo al verificar estado de módulo '" + moduleName + "' para rubro ID " + businessTypeId, e);
        }

        // Comportamiento por defecto para evitar bloqueos del sistema en caso de registros incompletos
        return true;
    }
}
