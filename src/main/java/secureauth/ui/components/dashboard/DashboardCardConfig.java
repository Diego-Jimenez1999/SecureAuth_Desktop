package secureauth.ui.components.dashboard;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class DashboardCardConfig {
    private static final Logger LOGGER = Logger.getLogger(DashboardCardConfig.class.getName());
    private static final Path CONFIG_PATH = Path.of("dashboard_cards.config.txt");
    private static boolean schemaInitialized = false;

    private static synchronized void ensureSchema() {
        if (schemaInitialized) {
            return;
        }
        try (Connection conn = secureauth.config.DatabaseConnection.getConnection();
             Statement st = conn.createStatement()) {
            st.execute("""
                    CREATE TABLE IF NOT EXISTS app_settings (
                        setting_key VARCHAR(180) PRIMARY KEY,
                        setting_value TEXT NULL
                    )
                    """);
            schemaInitialized = true;
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Error initializing app_settings table schema", ex);
        }
    }

    private static String getValueFromDb(String key) {
        ensureSchema();
        try (Connection conn = secureauth.config.DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT setting_value FROM app_settings WHERE setting_key = ?")) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("setting_value");
                }
            }
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Error reading setting from DB: " + key, ex);
        }
        return null;
    }

    private static void saveValueToDb(String key, String value) {
        ensureSchema();
        try (Connection conn = secureauth.config.DatabaseConnection.getConnection()) {
            boolean exists = false;
            try (PreparedStatement selectPs = conn.prepareStatement("SELECT 1 FROM app_settings WHERE setting_key = ?")) {
                selectPs.setString(1, key);
                try (ResultSet rs = selectPs.executeQuery()) {
                    exists = rs.next();
                }
            }

            if (exists) {
                try (PreparedStatement updatePs = conn.prepareStatement("UPDATE app_settings SET setting_value = ? WHERE setting_key = ?")) {
                    updatePs.setString(1, value);
                    updatePs.setString(2, key);
                    updatePs.executeUpdate();
                }
            } else {
                try (PreparedStatement insertPs = conn.prepareStatement("INSERT INTO app_settings (setting_key, setting_value) VALUES (?, ?)")) {
                    insertPs.setString(1, key);
                    insertPs.setString(2, value);
                    insertPs.executeUpdate();
                }
            }
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Error saving setting to DB: " + key, ex);
        }
    }

    public static boolean isVisible(String cardId, boolean defaultValue) {
        String dbVal = getValueFromDb("card." + cardId + ".visible");
        if (dbVal != null) {
            return Boolean.parseBoolean(dbVal.trim());
        }

        if (!Files.exists(CONFIG_PATH)) {
            return defaultValue;
        }
        Properties props = new Properties();
        try (var reader = Files.newBufferedReader(CONFIG_PATH, StandardCharsets.UTF_8)) {
            props.load(reader);
            String val = props.getProperty("card." + cardId + ".visible");
            if (val != null) {
                return Boolean.parseBoolean(val.trim());
            }
        } catch (IOException ignored) {}
        return defaultValue;
    }

    public static String getTitle(String cardId, String defaultValue) {
        String dbVal = getValueFromDb("card." + cardId + ".title");
        if (dbVal != null) {
            return dbVal.trim();
        }

        if (!Files.exists(CONFIG_PATH)) {
            return defaultValue;
        }
        Properties props = new Properties();
        try (var reader = Files.newBufferedReader(CONFIG_PATH, StandardCharsets.UTF_8)) {
            props.load(reader);
            String val = props.getProperty("card." + cardId + ".title");
            if (val != null && !val.trim().isEmpty()) {
                return val.trim();
            }
        } catch (IOException ignored) {}
        return defaultValue;
    }

    public static void saveConfig(String cardId, boolean visible, String title) {
        saveValueToDb("card." + cardId + ".visible", String.valueOf(visible));
        saveValueToDb("card." + cardId + ".title", title);

        Properties props = new Properties();
        if (Files.exists(CONFIG_PATH)) {
            try (var reader = Files.newBufferedReader(CONFIG_PATH, StandardCharsets.UTF_8)) {
                props.load(reader);
            } catch (IOException ignored) {}
        }
        props.setProperty("card." + cardId + ".visible", String.valueOf(visible));
        props.setProperty("card." + cardId + ".title", title);
        try (var writer = Files.newBufferedWriter(CONFIG_PATH, StandardCharsets.UTF_8)) {
            props.store(writer, "Dashboard Cards Configuration");
        } catch (IOException ignored) {}
    }
}
