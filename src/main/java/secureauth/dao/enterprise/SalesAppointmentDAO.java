package secureauth.dao.enterprise;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import secureauth.config.DatabaseConnection;
import secureauth.model.SalesAppointment;

/**
 * DAO JDBC para citas de peluquería, baño, spa y servicios caninos (Sales Appointments).
 */
public class SalesAppointmentDAO {

    /**
     * Crea la tabla {@code citas_servicio} si no existe.
     *
     * @throws SQLException si falla la creación del esquema
     */
    public void ensureSchema() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection(); Statement st = conn.createStatement()) {
            st.execute("""
                    CREATE TABLE IF NOT EXISTS citas_servicio (
                        id_cita INT AUTO_INCREMENT PRIMARY KEY,
                        nombre_dueno VARCHAR(160) NOT NULL,
                        nombre_perro VARCHAR(120) NOT NULL,
                        raza VARCHAR(120) NULL,
                        telefono VARCHAR(60) NOT NULL,
                        servicio VARCHAR(160) NOT NULL,
                        fecha_servicio DATE NOT NULL,
                        hora_servicio TIME NOT NULL,
                        hora_recogida TIME NOT NULL,
                        observaciones VARCHAR(700) NULL,
                        estado VARCHAR(40) NOT NULL,
                        fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                    )
                    """);
        }
    }

    /**
     * Inserta una cita usando una conexión transaccional existente.
     *
     * @param conn conexión JDBC activa
     * @param cita cita validada
     * @throws SQLException si falla el insert
     */
    public void insert(Connection conn, SalesAppointment cita) throws SQLException {
        String sql = """
                INSERT INTO citas_servicio(nombre_dueno, nombre_perro, raza, telefono, servicio,
                                           fecha_servicio, hora_servicio, hora_recogida, observaciones, estado)
                VALUES(?,?,?,?,?,?,?,?,?,?)
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cita.nombreDueno());
            ps.setString(2, cita.nombrePerro());
            ps.setString(3, cita.raza());
            ps.setString(4, cita.telefono());
            ps.setString(5, cita.servicio());
            ps.setDate(6, Date.valueOf(cita.fechaServicio()));
            ps.setTime(7, Time.valueOf(cita.horaServicio()));
            ps.setTime(8, Time.valueOf(cita.horaRecogida()));
            ps.setString(9, cita.observaciones());
            ps.setString(10, cita.estado());
            ps.executeUpdate();
        }
    }

    /**
     * Inserta una cita con conexión propia.
     *
     * @param cita cita validada
     * @throws SQLException si falla el registro
     */
    public void insert(SalesAppointment cita) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            insert(conn, cita);
        }
    }

    /**
     * Lista citas activas de la agenda legacy para una fecha.
     *
     * @param date fecha a consultar
     * @return citas ordenadas por hora
     * @throws SQLException si falla la consulta
     */
    public List<SalesAppointment> findActiveByDate(LocalDate date) throws SQLException {
        ensureSchema();
        String sql = """
                SELECT id_cita, nombre_dueno, nombre_perro, raza, telefono, servicio,
                       fecha_servicio, hora_servicio, hora_recogida, observaciones, estado
                FROM citas_servicio
                WHERE fecha_servicio = ?
                  AND UPPER(estado) NOT IN ('CANCELADA', 'CANCELADO')
                ORDER BY hora_servicio
                """;
        List<SalesAppointment> appointments = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    appointments.add(new SalesAppointment(
                            rs.getInt("id_cita"),
                            rs.getString("nombre_dueno"),
                            rs.getString("nombre_perro"),
                            rs.getString("raza"),
                            rs.getString("telefono"),
                            rs.getString("servicio"),
                            rs.getDate("fecha_servicio").toLocalDate(),
                            rs.getTime("hora_servicio").toLocalTime(),
                            rs.getTime("hora_recogida").toLocalTime(),
                            rs.getString("observaciones"),
                            rs.getString("estado")));
                }
            }
        }
        return appointments;
    }
}
