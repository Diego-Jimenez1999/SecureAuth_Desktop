package secureauth.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import secureauth.config.DatabaseConnection;
import secureauth.model.Owner;
import secureauth.shared.session.SessionManager;

/**
 * DAO (Data Access Object) para gestionar las consultas y operaciones
 * de los dueños o clientes directamente en la base de datos (tabla owners).
 *
 * @author Diego Alexander Gaviria Jimenez
 */
public class OwnerDAO {
    private final SessionManager sessionManager = SessionManager.getInstance();

    /**
     * Recupera todos los dueños registrados en la base de datos.
     *
     * @return Una lista con todos los objetos Owner, ordenada alfabéticamente por nombre.
     */
    public List<Owner> findAll() {
        final String sql = "SELECT id, nombre_completo, telefono, correo, direccion FROM owners WHERE business_id = ? ORDER BY nombre_completo";
        List<Owner> owners = new ArrayList<>();

        // Usamos try-with-resources para que las conexiones se cierren solas
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sessionManager.getCurrentBusinessId());
            try (ResultSet rs = ps.executeQuery()) {
            
                while (rs.next()) {
                    owners.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error consultando la lista de dueños.", e);
        }
        return owners;
    }

    /**
     * Busca un dueño específico usando su identificador único (id).
     *
     * @param id El número de identificación del dueño en la base de datos.
     * @return El objeto Owner si lo encuentra, o null si no existe.
     */
    public Owner findById(int id) {
        final String sql = "SELECT id, nombre_completo, telefono, correo, direccion FROM owners WHERE id = ? AND business_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, id);
            ps.setInt(2, sessionManager.getCurrentBusinessId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error buscando al dueño por su id.", e);
        }
        return null;
    }

    /**
     * Guarda un nuevo dueño/cliente en la base de datos.
     *
     * @param owner El objeto con los datos del cliente que queremos registrar.
     */
    public void insert(Owner owner) {
        final String sql = "INSERT INTO owners (business_id, nombre_completo, telefono, correo, direccion) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, sessionManager.getCurrentBusinessId());
            ps.setString(2, owner.getNombreCompleto());
            ps.setString(3, owner.getTelefono());
            ps.setString(4, owner.getCorreo());
            ps.setString(5, owner.getDireccion());
            ps.executeUpdate(); // Ejecuta la inserción
        } catch (SQLException e) {
            throw new RuntimeException("Error al intentar guardar un nuevo dueño.", e);
        }
    }

    /**
     * Cuenta cuántos dueños o clientes se han registrado en el mes actual.
     * Ideal para alimentar las métricas o gráficas del dashboard.
     *
     * @return La cantidad de clientes nuevos en este mes. Retorna 0 si hay algún error 
     *         (por ejemplo, si la tabla o la columna fecha_registro aún no existen).
     */
    public int countNewThisMonth() {
        final String sql = """
                SELECT COUNT(*) FROM owners
                WHERE business_id = ?
                AND YEAR(created_at)  = YEAR(CURRENT_DATE())
                AND MONTH(created_at) = MONTH(CURRENT_DATE())
                """;
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sessionManager.getCurrentBusinessId());
            try (ResultSet rs = ps.executeQuery()) {

                return rs.next() ? rs.getInt(1) : 0;
            }

        } catch (SQLException e) {
            // Se traga la excepción a propósito para que el dashboard no se caiga 
            // si la base de datos todavía se está configurando.
            return 0;
        }
    }

    /**
     * Método ayudante (helper) para convertir una fila que devuelve la base de datos 
     * en un objeto Java tipo Owner. Así no repetimos este bloque de código en cada consulta.
     *
     * @param rs El ResultSet que trae los datos de la consulta SQL.
     * @return Un objeto Owner con todos sus datos armados.
     * @throws SQLException Si algo falla al leer las columnas.
     */
    private Owner mapRow(ResultSet rs) throws SQLException {
        return new Owner(
                rs.getInt("id"),
                rs.getString("nombre_completo"),
                rs.getString("telefono"),
                rs.getString("correo"),
                rs.getString("direccion"));
    }
}
