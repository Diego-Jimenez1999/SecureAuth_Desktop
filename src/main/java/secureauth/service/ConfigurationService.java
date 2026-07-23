package secureauth.service;

import java.io.IOException;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import secureauth.config.DatabaseConnection;
import secureauth.ui.config.ApplicationVisualSettings;

/**
 * Servicio encargado de gestionar y centralizar la configuración del sistema.
 *
 * Carga configuraciones de las tablas 'app_settings' y 'branding_config',
 * validando los valores. Utiliza 'secureauth.config.txt' como mecanismo de respaldo.
 */
public class ConfigurationService {

    private static final Logger LOGGER = Logger.getLogger(ConfigurationService.class.getName());
    private static final ConfigurationService INSTANCE = new ConfigurationService();

    private final ApplicationVisualSettings localSettings;

    private ConfigurationService() {
        this.localSettings = ApplicationVisualSettings.load();
    }

    public static ConfigurationService getInstance() {
        return INSTANCE;
    }

    public synchronized String getSetting(String key, String defaultValue) {
        // First try to load from Database (app_settings table)
        String sql = "SELECT setting_value FROM app_settings WHERE setting_key = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String val = rs.getString("setting_value");
                    if (val != null && !val.trim().isEmpty()) {
                        return val.trim();
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "Error al cargar setting " + key + " de base de datos. Usando fallback.", e);
        }

        // Fallback to local config file property mapping
        return getLocalFallbackSetting(key, defaultValue);
    }

    public synchronized void setSetting(String key, String value, String description) {
        // Validate setting
        validateSettingValue(key, value);

        // Update in database
        String sql = "INSERT INTO app_settings(setting_key, setting_value, description) VALUES(?,?,?) "
                   + "ON DUPLICATE KEY UPDATE setting_value=VALUES(setting_value), description=VALUES(description)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            ps.setString(2, value);
            ps.setString(3, description);
            ps.executeUpdate();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "No se pudo persistir setting en base de datos. Se usará el archivo local.", e);
        }

        // Also update local settings and save to file for fallback
        updateLocalSetting(key, value);
        try {
            localSettings.save();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "No se pudo guardar el archivo de respaldo local.", e);
        }
    }

    private void validateSettingValue(String key, String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("El valor de configuración no puede estar vacío.");
        }
        if (key.contains("iva") || key.contains("tax") || key.contains("descuento")) {
            try {
                double val = Double.parseDouble(value);
                if (val < 0) {
                    throw new IllegalArgumentException("Los valores porcentuales no pueden ser negativos.");
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Valor numérico inválido para: " + key);
            }
        }
        if (key.contains("intervalo") || key.contains("duracion") || key.contains("stock_min") || key.contains("sesion")) {
            try {
                int val = Integer.parseInt(value);
                if (val < 0) {
                    throw new IllegalArgumentException("Los valores enteros no pueden ser negativos.");
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Valor entero inválido para: " + key);
            }
        }
    }

    private String getLocalFallbackSetting(String key, String defaultValue) {
        return switch (key) {
            case "branding" -> localSettings.getBranding();
            case "primary_color" -> localSettings.getPrimaryColor();
            case "secondary_color" -> localSettings.getSecondaryColor();
            case "tertiary_color" -> localSettings.getTertiaryColor();
            case "tax" -> String.valueOf(localSettings.getTax());
            case "currency" -> localSettings.getCurrency();
            case "empresa_nombre" -> localSettings.getEmpresaNombre();
            case "empresa_direccion" -> localSettings.getEmpresaDireccion();
            case "empresa_ciudad" -> localSettings.getEmpresaCiudad();
            case "empresa_telefono" -> localSettings.getEmpresaTelefono();
            case "empresa_correo" -> localSettings.getEmpresaCorreo();
            case "agenda_apertura" -> localSettings.getAgendaApertura();
            case "agenda_cierre" -> localSettings.getAgendaCierre();
            case "agenda_intervalo" -> String.valueOf(localSettings.getAgendaIntervalo());
            case "agenda_duracion" -> String.valueOf(localSettings.getAgendaDuracion());
            case "ventas_iva" -> String.valueOf(localSettings.getVentasIva());
            case "ventas_moneda" -> localSettings.getVentasMoneda();
            case "ventas_pago_defecto" -> localSettings.getVentasPagoDefecto();
            case "inventario_stock_min" -> String.valueOf(localSettings.getInventarioStockMin());
            case "usuarios_roles" -> localSettings.getUsuariosRoles();
            case "usuarios_permisos" -> localSettings.getUsuariosPermisos();
            case "visible_metrics_cards" -> localSettings.getVisibleMetricsCards();
            case "ai_model" -> localSettings.getAiModel();
            case "ai_active" -> String.valueOf(localSettings.isAiActive());
            default -> defaultValue;
        };
    }

    private void updateLocalSetting(String key, String value) {
        switch (key) {
            case "visible_metrics_cards" -> localSettings.setVisibleMetricsCards(value);
            case "branding" -> localSettings.setBranding(value);
            case "primary_color" -> localSettings.setPrimaryColor(value);
            case "secondary_color" -> localSettings.setSecondaryColor(value);
            case "tertiary_color" -> localSettings.setTertiaryColor(value);
            case "tax" -> localSettings.setTax(Double.parseDouble(value));
            case "currency" -> localSettings.setCurrency(value);
            case "empresa_nombre" -> localSettings.setEmpresaNombre(value);
            case "empresa_direccion" -> localSettings.setEmpresaDireccion(value);
            case "empresa_ciudad" -> localSettings.setEmpresaCiudad(value);
            case "empresa_telefono" -> localSettings.setEmpresaTelefono(value);
            case "empresa_correo" -> localSettings.setEmpresaCorreo(value);
            case "agenda_apertura" -> localSettings.setAgendaApertura(value);
            case "agenda_cierre" -> localSettings.setAgendaCierre(value);
            case "agenda_intervalo" -> localSettings.setAgendaIntervalo(Integer.parseInt(value));
            case "agenda_duracion" -> localSettings.setAgendaDuracion(Integer.parseInt(value));
            case "ventas_iva" -> localSettings.setVentasIva(Double.parseDouble(value));
            case "ventas_moneda" -> localSettings.setVentasMoneda(value);
            case "ventas_pago_defecto" -> localSettings.setVentasPagoDefecto(value);
            case "inventario_stock_min" -> localSettings.setInventarioStockMin(Integer.parseInt(value));
            case "usuarios_roles" -> localSettings.setUsuariosRoles(value);
            case "usuarios_permisos" -> localSettings.setUsuariosPermisos(value);
            case "ai_model" -> localSettings.setAiModel(value);
            case "ai_active" -> localSettings.setAiActive(Boolean.parseBoolean(value));
        }
    }
}
