package secureauth.dao.enterprise;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
                SELECT id_actividad, descripcion, fecha_hora, tipo, COALESCE(usuario, ''),
                       DATE_FORMAT(fecha_hora, '%Y-%m-%d') as fecha_real,
                       DATE_FORMAT(fecha_hora, '%H:%i:%S') as hora_real,
                       DATE_FORMAT(fecha_hora, '%Y-%m-%d %H:%i:%S') as timestamp_real
                FROM actividad_reciente
                ORDER BY fecha_hora DESC
                LIMIT ?
                """;
        List<ActividadReciente> rows = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new ActividadReciente(
                            rs.getInt(1),
                            rs.getString(2),
                            rs.getTimestamp(3).toLocalDateTime(),
                            rs.getString(4),
                            rs.getString(5),
                            rs.getString(6),
                            rs.getString(7),
                            rs.getString(8)
                    ));
                }
            }
        }
        return rows;
    }

    /**
     * Consulta registros de auditoría con filtros avanzados de búsqueda, módulo, fecha y usuario.
     *
     * @param query búsqueda por descripción
     * @param moduleFilter filtro de módulo (VENTAS, INVENTARIO, CITAS, SERVICIOS, SISTEMA)
     * @param dateFilter filtro de fecha (HOY, SEMANA, MES, ANIO, TODAS)
     * @param userFilter filtro por usuario (nombre o email)
     * @return lista de actividades/auditoría
     * @throws SQLException si falla la base de datos
     */
    public List<ActividadReciente> findAdvanced(String query, String moduleFilter, String dateFilter, String userFilter) throws SQLException {
        ensureSchema();
        StringBuilder sql = new StringBuilder("""
                SELECT id_actividad, descripcion, fecha_hora, tipo, COALESCE(usuario, ''),
                       DATE_FORMAT(fecha_hora, '%Y-%m-%d') as fecha_real,
                       DATE_FORMAT(fecha_hora, '%H:%i:%S') as hora_real,
                       DATE_FORMAT(fecha_hora, '%Y-%m-%d %H:%i:%S') as timestamp_real
                FROM actividad_reciente
                WHERE 1=1
                """);
        List<Object> params = new ArrayList<>();

        if (query != null && !query.trim().isEmpty()) {
            sql.append(" AND descripcion LIKE ?");
            params.add("%" + query.trim() + "%");
        }

        if (moduleFilter != null && !moduleFilter.equalsIgnoreCase("TODAS")) {
            sql.append(" AND tipo = ?");
            params.add(moduleFilter.trim().toUpperCase(Locale.ROOT));
        }

        if (userFilter != null && !userFilter.trim().isEmpty()) {
            sql.append(" AND usuario LIKE ?");
            params.add("%" + userFilter.trim() + "%");
        }

        if (dateFilter != null) {
            switch (dateFilter.toUpperCase()) {
                case "HOY" -> sql.append(" AND DATE(fecha_hora) = CURRENT_DATE()");
                case "SEMANA" -> sql.append(" AND YEARWEEK(fecha_hora, 1) = YEARWEEK(CURRENT_DATE(), 1)");
                case "MES" -> sql.append(" AND YEAR(fecha_hora) = YEAR(CURRENT_DATE()) AND MONTH(fecha_hora) = MONTH(CURRENT_DATE())");
                case "ANIO", "AÑO" -> sql.append(" AND YEAR(fecha_hora) = YEAR(CURRENT_DATE())");
            }
        }

        sql.append(" ORDER BY fecha_hora DESC");

        List<ActividadReciente> results = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(new ActividadReciente(
                            rs.getInt(1),
                            rs.getString(2),
                            rs.getTimestamp(3).toLocalDateTime(),
                            rs.getString(4),
                            rs.getString(5),
                            rs.getString(6),
                            rs.getString(7),
                            rs.getString(8)
                    ));
                }
            }
        }
        return results;
    }
}
