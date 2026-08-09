package secureauth.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Gestiona el pool de conexiones a la base de datos MySQL.
 *
 * <p>Intenta usar <strong>HikariCP</strong> si la librería está disponible en el
 * classpath. Si el pool no puede inicializarse, cae automáticamente a
 * {@link java.sql.DriverManager} como respaldo.</p>
 *
 * <h2>Configuración</h2>
 * <p>Las credenciales se leen de propiedades JVM, variables de entorno o archivo
 * local {@code .env}:
 * <ul>
 *   <li>{@code SECUREAUTH_DB_URL}  — por defecto {@code jdbc:mysql://localhost:3306/secureauth}</li>
 *   <li>{@code SECUREAUTH_DB_USER} — por defecto {@code root}</li>
 *   <li>{@code SECUREAUTH_DB_PASSWORD} — por defecto {@code ""} (vacío; configura la variable en entornos reales)</li>
 * </ul>
 * </p>
 *
 * @author Diego
 * @version 2.3 — Pool HikariCP tipado y configuracion local segura.
 */
public class DatabaseConnection {

    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());
    private static final Map<String, String> LOCAL_ENV = loadLocalEnv();

    private static final String URL      = setting("SECUREAUTH_DB_URL",      "jdbc:mysql://localhost:3306/secureauth");
    private static final String USER     = setting("SECUREAUTH_DB_USER",     null);
    private static final String PASSWORD = setting("SECUREAUTH_DB_PASSWORD", null);

    static {
        validateCredentials();
    }

    private static void validateCredentials() {
        java.util.List<String> missing = new java.util.ArrayList<>();
        if (USER == null || USER.trim().isEmpty()) {
            missing.add("SECUREAUTH_DB_USER");
        }
        if (PASSWORD == null || PASSWORD.trim().isEmpty()) {
            missing.add("SECUREAUTH_DB_PASSWORD");
        }
        if (!missing.isEmpty()) {
            String missingStr = String.join(", ", missing);
            throw new IllegalStateException(
                "Configuración de base de datos incompleta. Faltan las siguientes variables: " + missingStr + ".\n" +
                "Defínelas como variable de entorno, system property (-DSECUREAUTH_DB_USER=...) o en un archivo .env en la raíz del proyecto.\n" +
                "Ver .env.example para la lista completa de variables esperadas."
            );
        }
    }

    private static final long CONNECTION_TIMEOUT_MS = 5_000L;
    private static final long VALIDATION_TIMEOUT_MS = 3_000L;

    /** Pool HikariCP compartido por toda la aplicacion. */
    private static final HikariDataSource DATA_SOURCE = buildHikariPool();

    private DatabaseConnection() {}

    /**
     * Devuelve una conexión del pool (o una conexión directa si HikariCP no está disponible).
     *
     * @return {@link Connection} lista para usar
     * @throws SQLException si no se puede obtener la conexión
     */
    public static Connection getConnection() throws SQLException {
        if (DATA_SOURCE != null) {
            try {
                return DATA_SOURCE.getConnection();
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, "Error de conexion completo (sin password): URL=" + URL + ", usuario=" + USER, ex);
                throw new SQLException(buildConnectionErrorMessage(), ex);
            }
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver MySQL no encontrado", e);
        }
        try {
            return java.sql.DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error de conexion completo (sin password): URL=" + URL + ", usuario=" + USER, ex);
            throw new SQLException(buildConnectionErrorMessage(), ex);
        }
    }

    /**
     * Construye el pool HikariCP. Si no puede inicializarse, la aplicación cae a
     * DriverManager para preservar compatibilidad de ejecución en entornos legacy.
     */
    private static HikariDataSource buildHikariPool() {
        try {
            HikariConfig cfg = new HikariConfig();
            cfg.setJdbcUrl(URL);
            cfg.setUsername(USER);
            cfg.setPassword(PASSWORD);
            cfg.setMaximumPoolSize(10);
            cfg.setMinimumIdle(0);
            cfg.setConnectionTimeout(CONNECTION_TIMEOUT_MS);
            cfg.setValidationTimeout(VALIDATION_TIMEOUT_MS);
            cfg.setIdleTimeout(600_000L);
            cfg.setMaxLifetime(1_800_000L);
            cfg.setInitializationFailTimeout(-1L);
            cfg.setRegisterMbeans(false);
            cfg.setPoolName("SecureAuthPool");

            HikariDataSource ds = new HikariDataSource(cfg);
            LOGGER.info("HikariCP pool inicializado correctamente.");
            return ds;
        } catch (RuntimeException e) {
            LOGGER.log(Level.WARNING, "No se pudo inicializar HikariCP. Usando DriverManager.", e);
            return null;
        }
    }

    private static String buildConnectionErrorMessage() {
        return "No se pudo conectar a MySQL con URL=" + URL
                + ". Verifica SECUREAUTH_DB_URL, SECUREAUTH_DB_USER y SECUREAUTH_DB_PASSWORD.";
    }

    private static String setting(String key, String defaultValue) {
        String systemProperty = System.getProperty(key);
        if (hasText(systemProperty)) {
            return systemProperty.trim();
        }

        String environmentValue = System.getenv(key);
        if (hasText(environmentValue)) {
            return environmentValue.trim();
        }

        String localValue = LOCAL_ENV.get(key);
        if (hasText(localValue)) {
            return localValue.trim();
        }

        return defaultValue;
    }

    private static Map<String, String> loadLocalEnv() {
        Path envPath = Path.of(".env");
        if (!Files.isRegularFile(envPath)) {
            return Map.of();
        }

        Map<String, String> values = new HashMap<>();
        try {
            for (String line : Files.readAllLines(envPath)) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }

                int separator = trimmed.indexOf('=');
                if (separator <= 0) {
                    continue;
                }

                String key = trimmed.substring(0, separator).trim();
                String value = stripQuotes(trimmed.substring(separator + 1).trim());
                if (!key.isEmpty()) {
                    values.put(key, value);
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "No se pudo leer archivo .env local. Se usaran variables del sistema.", e);
        }
        return values;
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static String stripQuotes(String value) {
        if (value.length() >= 2
                && ((value.startsWith("\"") && value.endsWith("\""))
                || (value.startsWith("'") && value.endsWith("'")))) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }
}
