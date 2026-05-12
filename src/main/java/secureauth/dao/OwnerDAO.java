package secureauth.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import secureauth.config.DatabaseConnection;
import secureauth.model.Owner;

/**
 * DAO para consulta de dueños en la tabla owners.
 *
 * @author Diego Alexander Gaviria Jimenez
 */
public class OwnerDAO {

    /**
     * Recupera todos los dueños registrados.
     *
     * @return lista de dueños
     */
    public List<Owner> findAll() {
        final String sql = "SELECT id, nombre_completo, telefono, correo, direccion FROM owners ORDER BY nombre_completo";
        List<Owner> owners = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                owners.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error consultando owners.", e);
        }
        return owners;
    }

    /**
     * Busca un dueño por su id.
     *
     * @param id identificador del dueño
     * @return dueño encontrado o null si no existe
     */
    public Owner findById(int id) {
        final String sql = "SELECT id, nombre_completo, telefono, correo, direccion FROM owners WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error consultando owner por id.", e);
        }
        return null;
    }

    private Owner mapRow(ResultSet rs) throws SQLException {
        return new Owner(
                rs.getInt("id"),
                rs.getString("nombre_completo"),
                rs.getString("telefono"),
                rs.getString("correo"),
                rs.getString("direccion"));
    }
}
