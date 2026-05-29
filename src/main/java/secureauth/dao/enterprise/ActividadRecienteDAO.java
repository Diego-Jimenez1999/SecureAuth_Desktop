package secureauth.dao.enterprise;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import secureauth.config.DatabaseConnection;
import secureauth.model.ActividadReciente;

/**
 * DAO JDBC para la tabla {@code actividad_reciente}.
 *
 * <p>Centraliza la escritura de eventos generados por ventas, inventario y
 * agendamiento para que el Home pueda mostrarlos en tiempo real.</p>
 */
public class ActividadRecienteDAO {

    /**
     * Crea la tabla de actividad reciente si no existe.
     *
     * @throws SQLException si falla la inicialización del esquema
     */
    public void ensureSchema() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection(); Statement st = conn.createStatement()) {
            st.execute("""
                    CREATE TABLE IF NOT EXISTS actividad_reciente (
                        id_actividad INT AUTO_INCREMENT PRIMARY KEY,
                        descripcion VARCHAR(300) NOT NULL,
                        fecha_hora TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        tipo VARCHAR(40) NOT NULL,
                        usuario VARCHAR(180) NULL
                    )
                    """);
        }
    }

    /**
     * Registra una actividad usando una conexión transaccional existente.
     *
     * @param conn conexión JDBC activa
     * @param descripcion descripción del evento
     * @param tipo tipo de evento
     * @param usuario usuario responsable
     * @throws SQLException si falla el insert
     */
    public void insert(Connection conn, String descripcion, String tipo, String usuario) throws SQLException {
        String sql = "INSERT INTO actividad_reciente(descripcion, tipo, usuario) VALUES(?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, descripcion);
            ps.setString(2, tipo);
            ps.setString(3, usuario);
            ps.executeUpdate();
        }
    }

    /**
     * Registra una actividad abriendo su propia conexión.
     *
     * @param descripcion descripción del evento
     * @param tipo tipo de evento
     * @param usuario usuario responsable
     * @throws SQLException si falla el insert
     */
    public void insert(String descripcion, String tipo, String usuario) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            insert(conn, descripcion, tipo, usuario);
        }
    }

    /**
     * Consulta las actividades más recientes.
     *
     * @param limit máximo de filas
     * @return actividades ordenadas de más reciente a más antigua
     * @throws SQLException si falla la consulta
     */
    public List<ActividadReciente> findRecent(int limit) throws SQLException {
        String sql = """
                SELECT id_actividad, descripcion, fecha_hora, tipo, COALESCE(usuario, '')
                FROM actividad_reciente
                ORDER BY fecha_hora DESC
                LIMIT ?
                """;
        List<ActividadReciente> rows = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new ActividadReciente(rs.getInt(1), rs.getString(2),
                            rs.getTimestamp(3).toLocalDateTime(), rs.getString(4), rs.getString(5)));
                }
            }
        }
        return rows;
    }
}
