package secureauth.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import secureauth.config.DatabaseConnection;
import secureauth.model.Mascota;

/**
 * DAO para la entidad Mascota.
 */
public class MascotaDAO {

    public boolean insert(Mascota mascota) {
        String sql = """
                INSERT INTO mascotas (
                    nombre, tipo, raza, edad, peso, sexo, frecuencia_alimentacion,
                    descripcion_cuidados, estado_salud, nombre_dueno, telefono_dueno,
                    correo_dueno, direccion_dueno, ruta_imagen
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, mascota.getNombre());
            ps.setString(2, mascota.getTipo());
            ps.setString(3, mascota.getRaza());
            ps.setInt(4, mascota.getEdad());
            ps.setDouble(5, mascota.getPeso());
            ps.setString(6, mascota.getSexo());
            ps.setString(7, mascota.getFrecuenciaAlimentacion());
            ps.setString(8, mascota.getDescripcionCuidados());
            ps.setString(9, mascota.getEstadoSalud());
            ps.setString(10, mascota.getNombreDueno());
            ps.setString(11, mascota.getTelefonoDueno());
            ps.setString(12, mascota.getCorreoDueno());
            ps.setString(13, mascota.getDireccionDueno());
            ps.setString(14, mascota.getRutaImagen());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
