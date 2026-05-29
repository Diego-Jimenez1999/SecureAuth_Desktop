package secureauth.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import secureauth.config.DatabaseConnection;
import secureauth.config.SchemaInspector;
import secureauth.model.Pet;

/**
 * DAO encargado de persistir la entidad {@link Pet} en la tabla {@code pets}.
 *
 * @author Diego Alexander Gaviria Jimenez
 */
public class PetDAO {

    private static final Logger LOGGER = Logger.getLogger(PetDAO.class.getName());

    /**
     * Crea y migra la tabla de mascotas necesaria para el módulo de registro.
     *
     * <p>Este método hace que el módulo sea autónomo: si una instalación tiene
     * {@code owners} pero no {@code pets}, el registro no dependerá de scripts
     * manuales externos.</p>
     */
    public void ensureSchema() {
        try (Connection conn = DatabaseConnection.getConnection(); Statement st = conn.createStatement()) {
            st.execute("""
                    CREATE TABLE IF NOT EXISTS owners (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        nombre_completo VARCHAR(180) NOT NULL,
                        telefono VARCHAR(60),
                        correo VARCHAR(160),
                        direccion VARCHAR(220),
                        fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                    )
                    """);
            st.execute("""
                    CREATE TABLE IF NOT EXISTS pets (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        owner_id INT NOT NULL,
                        nombre_mascota VARCHAR(140) NOT NULL,
                        raza VARCHAR(120) NOT NULL,
                        edad VARCHAR(60),
                        peso DECIMAL(8,2) NOT NULL,
                        sexo VARCHAR(20) NOT NULL,
                        frecuencia_alimentacion VARCHAR(180),
                        tipo_alimento VARCHAR(180),
                        estado_salud VARCHAR(80),
                        vacunas VARCHAR(300),
                        cuidados_especiales TEXT,
                        notas_adicionales TEXT,
                        imagen_path VARCHAR(500),
                        fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        CONSTRAINT fk_pets_owner FOREIGN KEY (owner_id) REFERENCES owners(id)
                    )
                    """);
            migratePetsTable(conn, st);
        } catch (SQLException e) {
            throw new PetDataAccessException("No se pudo inicializar la tabla de mascotas.", e);
        }
    }

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
        ensureSchema();
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
            throw custom;
        }
    }

    private void migratePetsTable(Connection conn, Statement st) throws SQLException {
        addColumnIfMissing(conn, st, "pets", "edad", "VARCHAR(60)");
        addColumnIfMissing(conn, st, "pets", "frecuencia_alimentacion", "VARCHAR(180)");
        addColumnIfMissing(conn, st, "pets", "tipo_alimento", "VARCHAR(180)");
        addColumnIfMissing(conn, st, "pets", "estado_salud", "VARCHAR(80)");
        addColumnIfMissing(conn, st, "pets", "vacunas", "VARCHAR(300)");
        addColumnIfMissing(conn, st, "pets", "cuidados_especiales", "TEXT");
        addColumnIfMissing(conn, st, "pets", "notas_adicionales", "TEXT");
        addColumnIfMissing(conn, st, "pets", "imagen_path", "VARCHAR(500)");
        addColumnIfMissing(conn, st, "pets", "fecha_registro", "TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP");
        if (SchemaInspector.columnExists(conn, "pets", "sexo")) {
            st.execute("ALTER TABLE pets MODIFY COLUMN sexo VARCHAR(20) NOT NULL");
        }
    }

    private void addColumnIfMissing(Connection conn, Statement st, String table, String column, String definition)
            throws SQLException {
        if (!SchemaInspector.columnExists(conn, table, column)) {
            st.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
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
