package secureauth.dao.enterprise;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;

import secureauth.config.DatabaseConnection;
import secureauth.model.CitaServicio;

/**
 * DAO JDBC para citas de peluquería, baño, spa y servicios caninos.
 */
public class AgendaServicioDAO {

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
    public void insert(Connection conn, CitaServicio cita) throws SQLException {
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
    public void insert(CitaServicio cita) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            insert(conn, cita);
        }
    }
}
