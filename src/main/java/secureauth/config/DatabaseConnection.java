package secureauth.config;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Gestiona el pool de conexiones a la base de datos MySQL.
 *
 * <p>Intenta usar <strong>HikariCP</strong> si la librería está disponible en el
 * classpath. Si no, cae automáticamente a {@link java.sql.DriverManager} como
 * respaldo, de modo que el proyecto compile y funcione aunque HikariCP no esté
 * aún en el {@code pom.xml}.</p>
 *
 * <h2>Configuración</h2>
 * <p>Las credenciales se leen de variables de entorno:
 * <ul>
 *   <li>{@code SECUREAUTH_DB_URL}  — por defecto {@code jdbc:mysql://localhost:3306/secureauth}</li>
 *   <li>{@code SECUREAUTH_DB_USER} — por defecto {@code root}</li>
 *   <li>{@code SECUREAUTH_DB_PASSWORD} — por defecto {@code ""} (vacío; configura la variable en producción)</li>
 * </ul>
 * </p>
 *
 * <h2>Agregar HikariCP al proyecto</h2>
 * <pre>
 * &lt;dependency&gt;
 *   &lt;groupId&gt;com.zaxxer&lt;/groupId&gt;
 *   &lt;artifactId&gt;HikariCP&lt;/artifactId&gt;
 *   &lt;version&gt;5.1.0&lt;/version&gt;
 * &lt;/dependency&gt;
 * </pre>
 *
 * @author Diego
 * @version 2.1 — Pool HikariCP con fallback a DriverManager.
 */
public class DatabaseConnection {

    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());

    private static final String URL      = System.getenv().getOrDefault("SECUREAUTH_DB_URL",      "jdbc:mysql://localhost:3306/secureauth");
    private static final String USER     = System.getenv().getOrDefault("SECUREAUTH_DB_USER",     "root");
    private static final String PASSWORD = System.getenv().getOrDefault("SECUREAUTH_DB_PASSWORD", "1234");

    /** Pool HikariCP — null si la librería no está disponible. */
    private static final Object DATA_SOURCE = tryBuildHikariPool();

    private DatabaseConnection() {}

    /**
     * Devuelve una conexión del pool (o una conexión directa si HikariCP no está disponible).
     *
     * @return {@link Connection} lista para usar
     * @throws SQLException si no se puede obtener la conexión
     */
    @SuppressWarnings("UseSpecificCatch")
    public static Connection getConnection() throws SQLException {
        if (DATA_SOURCE != null) {
            // HikariCP disponible → usar pool
            try {
                java.lang.reflect.Method m = DATA_SOURCE.getClass().getMethod("getConnection");
                return (Connection) m.invoke(DATA_SOURCE);
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Error obteniendo conexión de HikariCP, usando DriverManager", ex);
            }
        }
        // Fallback a DriverManager (sin pool)
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver MySQL no encontrado", e);
        }
        return java.sql.DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * Intenta construir un pool HikariCP via reflexión.
     * Si la clase no existe en el classpath, devuelve null silenciosamente.
     */
    @SuppressWarnings("UseSpecificCatch")
    private static Object tryBuildHikariPool() {
        try {
            Class<?> cfgClass  = Class.forName("com.zaxxer.hikari.HikariConfig");
            Class<?> dsClass   = Class.forName("com.zaxxer.hikari.HikariDataSource");

            Object cfg = cfgClass.getDeclaredConstructor().newInstance();

            cfgClass.getMethod("setJdbcUrl",          String.class).invoke(cfg, URL);
            cfgClass.getMethod("setUsername",          String.class).invoke(cfg, USER);
            cfgClass.getMethod("setPassword",          String.class).invoke(cfg, PASSWORD);
            cfgClass.getMethod("setMaximumPoolSize",   int.class).invoke(cfg, 10);
            cfgClass.getMethod("setMinimumIdle",       int.class).invoke(cfg, 2);
            cfgClass.getMethod("setConnectionTimeout", long.class).invoke(cfg, 30_000L);
            cfgClass.getMethod("setIdleTimeout",       long.class).invoke(cfg, 600_000L);
            cfgClass.getMethod("setMaxLifetime",       long.class).invoke(cfg, 1_800_000L);

            // Evita que HikariCP registre un shutdown hook que interfiera con Swing
            cfgClass.getMethod("setRegisterMbeans", boolean.class).invoke(cfg, false);

            Object ds = dsClass.getDeclaredConstructor(cfgClass).newInstance(cfg);
            LOGGER.info("HikariCP pool inicializado correctamente.");
            return ds;

        } catch (ClassNotFoundException e) {
            LOGGER.info("HikariCP no encontrado en classpath. Usando DriverManager (sin pool). " +
                        "Agrega HikariCP al pom.xml para mejorar el rendimiento.");
            return null;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "No se pudo inicializar HikariCP. Usando DriverManager.", e);
            return null;
        }
    }
}