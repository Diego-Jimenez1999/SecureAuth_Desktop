package secureauth.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import secureauth.config.DatabaseConnection;
import secureauth.model.Pet;

/**
 * DAO encargado de persistir la entidad {@link Pet} en la tabla {@code pets}.
 *
 * @author Diego Alexander Gaviria Jimenez
 */
public class PetDAO {

    private static final Logger LOGGER = Logger.getLogger(PetDAO.class.getName());

    /**
     * Inserta un registro de mascota en la base de datos.
     *
     * <p>Implementa {@link PreparedStatement} para prevenir inyección SQL y
     * utiliza {@code try-with-resources} para el cierre automático de recursos.</p>
     *
     * @param pet entidad mascota a persistir
     * @return {@code true} si la inserción afectó al menos una fila; en caso contrario {@code false}
     */
    public boolean insert(Pet pet) {
        final String sql = """
                INSERT INTO pets (
                    owner_id, nombre_mascota, raza, edad, peso, sexo,
                    frecuencia_alimentacion, tipo_alimento, estado_salud,
                    vacunas, cuidados_especiales, notas_adicionales, imagen_path
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, pet.getOwnerId());
            ps.setString(2, pet.getNombreMascota());
            ps.setString(3, pet.getRaza());
            ps.setString(4, pet.getEdad());
            ps.setDouble(5, pet.getPeso());
            ps.setString(6, pet.getSexo());
            ps.setString(7, pet.getFrecuenciaAlimentacion());
            ps.setString(8, pet.getTipoAlimento());
            ps.setString(9, pet.getEstadoSalud());
            ps.setString(10, pet.getVacunas());
            ps.setString(11, pet.getCuidadosEspeciales());
            ps.setString(12, pet.getNotasAdicionales());
            ps.setString(13, pet.getImagenPath());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            String message = "Error al insertar la mascota en la tabla pets.";
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("foreign key")) {
                message = "No se pudo registrar la mascota: owner_id no existe en la tabla owners.";
            } else if (e.getMessage() != null && e.getMessage().toLowerCase().contains("data truncated for column 'sexo'")) {
                message = "No se pudo registrar la mascota: el valor de sexo no coincide con el ENUM de la base de datos.";
            }
            PetDataAccessException custom = mapSqlException(message, e);
            LOGGER.log(Level.SEVERE, custom.getMessage(), custom);
            return false;
        }
    }

    /**
     * Convierte una {@link SQLException} en una excepción de dominio DAO.
     *
     * @param message contexto del error
     * @param cause excepción SQL original
     * @return excepción personalizada de acceso a datos
     */
    private PetDataAccessException mapSqlException(String message, SQLException cause) {
        return new PetDataAccessException(message, cause);
    }
}
