/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package secureauth.config;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Clase encargada de gestionar la conexión a la base de datos MySQL.
 *
 * <p>
 * Centraliza los parámetros de conexión y proporciona un método
 * reutilizable para obtener conexiones en toda la aplicación.
 * </p>
 *
 * <h2>Responsabilidades</h2>
 * <ul>
 *     <li>Crear conexiones a la base de datos</li>
 *     <li>Evitar duplicación de código de conexión</li>
 * </ul>
 *
 * <h2>Ejemplo de uso</h2>
 * <pre>
 * {@code
 * Connection conn = DatabaseConnection.getConnection();
 * }
 * </pre>
 *
 * @author Diego
 * @version 1.0
 */
public class DatabaseConnection {

    /** URL de conexión a MySQL */
    private static final String URL = System.getenv().getOrDefault("SECUREAUTH_DB_URL", "jdbc:mysql://localhost:3306/secureauth");

    /** Usuario de la base de datos */
    private static final String USER = System.getenv().getOrDefault("SECUREAUTH_DB_USER", "root");

    /** Contraseña de la base de datos */
    private static final String PASSWORD = System.getenv().getOrDefault("SECUREAUTH_DB_PASSWORD", "1234");

    /**
     * Obtiene una conexión a la base de datos.
     *
     * @return objeto {@link Connection}
     * @throws SQLException si ocurre un error de conexión
     */
    public static Connection getConnection() throws SQLException {

        try {
            // =========================
            // CARGA DEL DRIVER
            // =========================
            Class.forName("com.mysql.cj.jdbc.Driver");

            // =========================
            // CREACIÓN DE CONEXIÓN
            // =========================
            return DriverManager.getConnection(URL, USER, PASSWORD);

        } catch (ClassNotFoundException e) {
            // Error si el driver no está en el proyecto
            throw new SQLException("No se encontró el driver de MySQL", e);
        }
    }
}
