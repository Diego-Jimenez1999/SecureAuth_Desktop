package secureauth.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import secureauth.config.DatabaseConnection;
import secureauth.model.User;

/**
 * DAO de acceso a datos para la entidad User.
 *
 * LoginFrame → AuthController → AuthService → UserDAO → Database
 *
 * @author Diego Jiménez
 * @version 2.1 — ResultSet cerrado en try-with-resources en todos los métodos,
 *               countNewThisMonth() para métricas de dashboard.
 */
public class UserDAO {

    private static final Logger LOGGER = Logger.getLogger(UserDAO.class.getName());

    /**
     * DTO liviano para renderizar filas de trabajadores en tablas de gestión.
     * Evita exponer contraseñas u objetos pesados a la capa UI.
     */
    public static class WorkerRow {
        private final int id;
        private final String nombre;
        private final String apellido;
        private final String email;
        private final String genero;
        private final String rol;

        public WorkerRow(int id, String nombre, String apellido,
                        String email, String genero, String rol) {
            this.id = id;
            this.nombre = nombre;
            this.apellido = apellido;
            this.email = email;
            this.genero = genero;
            this.rol = rol;
        }

        public int getId()          { return id; }
        public String getNombre()   { return nombre; }
        public String getApellido() { return apellido; }
        public String getEmail()    { return email; }
        public String getGenero()   { return genero; }
        public String getRol()      { return rol; }
    }

    // =========================================================
    // CONSULTAS
    // =========================================================

    /**
     * Recupera la totalidad de los usuarios registrados en el sistema.
     *
     * @return Una {@link List} de objetos {@link User}.
     */
    public List<User> findAll() {
        List<User> lista = new ArrayList<>();
        String sql = "SELECT id, email, password, nombre, apellido, fecha_nacimiento, genero, rol_id FROM users";

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(mapResultSetToUser(rs));
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener todos los usuarios", e);
        }
        return lista;
    }

    /**
     * Realiza una búsqueda avanzada filtrando por nombre, apellido o email.
     *
     * @param texto Cadena de búsqueda.
     * @return Lista filtrada de usuarios.
     */
    public List<User> search(String texto) {
        List<User> lista = new ArrayList<>();
        String sql = """
                SELECT id, email, password, nombre, apellido, fecha_nacimiento, genero, rol_id FROM users
                WHERE nombre LIKE ?
                OR apellido LIKE ?
                OR email LIKE ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            String filtro = "%" + texto + "%";
            stmt.setString(1, filtro);
            stmt.setString(2, filtro);
            stmt.setString(3, filtro);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapResultSetToUser(rs));
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error en la búsqueda de usuarios", e);
        }
        return lista;
    }

    /**
     * Localiza un usuario por su ID.
     *
     * @param id El ID del usuario.
     * @return Objeto {@link User} o {@code null} si no existe.
     */
    public User findById(int id) {
        String sql = "SELECT id, email, password, nombre, apellido, fecha_nacimiento, genero, rol_id FROM users WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al buscar usuario por ID: " + id, e);
        }
        return null;
    }

    /**
     * Recupera un usuario por su correo electrónico.
     *
     * @param email Email a consultar.
     * @return Objeto {@link User} o {@code null} si no se encuentra.
     */
    public User findByEmail(String email) {
        String sql = "SELECT id, email, password, nombre, apellido, fecha_nacimiento, genero, rol_id FROM users WHERE email = ?";

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al buscar usuario por email", e);
        }
        return null;
    }

    /**
     * JOIN entre users y roles para obtener trabajadores con nombre de rol legible.
     *
     * @param query Filtro opcional de texto.
     * @return Lista de {@link WorkerRow}.
     */
    public List<WorkerRow> findAllWithRoleName(String query) {
        List<WorkerRow> workers = new ArrayList<>();
        String roleNameColumn = resolveRoleNameColumn();

        StringBuilder sql = new StringBuilder("""
                SELECT u.id, u.nombre, u.apellido, u.email, u.genero,
                    COALESCE(r.%s, 'Sin rol') AS rol
                FROM users u
                LEFT JOIN roles r ON r.id = u.rol_id
                """.formatted(roleNameColumn));

        boolean hasQuery = query != null && !query.trim().isEmpty();
        if (hasQuery) {
            sql.append(" WHERE u.nombre LIKE ? OR u.apellido LIKE ? OR u.email LIKE ? ");
        }
        sql.append(" ORDER BY u.id ASC");

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            if (hasQuery) {
                String filter = "%" + query.trim() + "%";
                stmt.setString(1, filter);
                stmt.setString(2, filter);
                stmt.setString(3, filter);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    workers.add(new WorkerRow(
                            rs.getInt("id"),
                            rs.getString("nombre"),
                            rs.getString("apellido"),
                            rs.getString("email"),
                            rs.getString("genero"),
                            rs.getString("rol")
                    ));
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al listar trabajadores con rol", e);
        }
        return workers;
    }

    /** Sobrecarga sin filtro — lista todos los trabajadores. */
    public List<WorkerRow> findAllWithRoleName() {
        return findAllWithRoleName(null);
    }

    /**
     * Cuenta usuarios registrados en el mes actual.
     * Usado por el dashboard de métricas.
     *
     * @return cantidad de usuarios nuevos este mes
     */
    public int countNewThisMonth() {
        String sql = """
                SELECT COUNT(*) FROM users
                WHERE YEAR(created_at)  = YEAR(CURRENT_DATE())
                AND   MONTH(created_at) = MONTH(CURRENT_DATE())
                """;
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {

            return rs.next() ? rs.getInt(1) : 0;

        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error contando usuarios nuevos del mes", e);
            return 0;
        }
    }

    // =========================================================
    // ESCRITURA
    // =========================================================

    /**
     * Persiste un nuevo usuario en la tabla {@code users}.
     *
     * @param user Objeto con los datos del nuevo usuario.
     * @return {@code true} si se insertó correctamente.
     */
    public boolean insert(User user) {
        Objects.requireNonNull(user, "El usuario no puede ser nulo para la inserción");
        String sql = "INSERT INTO users (nombre, apellido, email, password, genero, fecha_nacimiento, rol_id) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getNombre());
            ps.setString(2, user.getApellido());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getPassword());
            ps.setString(5, user.getGenero());
            ps.setDate(6, user.getFechaNacimiento() != null
                    ? Date.valueOf(user.getFechaNacimiento()) : null);
            ps.setInt(7, user.getRolId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al insertar usuario", e);
            return false;
        }
    }

    /**
     * Actualiza la contraseña de un usuario.
     *
     * @param userId         ID del usuario.
     * @param hashedPassword Nuevo hash BCrypt.
     * @return true si se actualizó
     */
    public boolean updatePassword(int userId, String hashedPassword) {
        String sql = "UPDATE users SET password = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hashedPassword);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al actualizar contraseña", e);
            return false;
        }
    }

    /**
     * Elimina físicamente un usuario de la base de datos.
     *
     * @param id ID del usuario a eliminar.
     */
    public void delete(int id) {
        String sql = "DELETE FROM users WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al eliminar usuario", e);
        }
    }

    /**
     * Actualiza todos los campos de un usuario existente.
     *
     * @param user Objeto con los datos actualizados.
     */
    public void update(User user) {
        Objects.requireNonNull(user, "El usuario no puede ser nulo para la actualización");
        String sql = "UPDATE users SET nombre=?, apellido=?, email=?, password=?, genero=?, fecha_nacimiento=?, rol_id=? WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getNombre());
            ps.setString(2, user.getApellido());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getPassword());
            ps.setString(5, user.getGenero());
            ps.setDate(6, user.getFechaNacimiento() != null
                    ? Date.valueOf(user.getFechaNacimiento()) : null);
            ps.setInt(7, user.getRolId());
            ps.setInt(8, user.getId());

            ps.executeUpdate();

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al actualizar usuario", e);
        }
    }

    // =========================================================
    // HELPERS PRIVADOS
    // =========================================================

    /**
     * Transforma una fila del ResultSet en un objeto {@link User}.
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        Date fechaNacimientoSQL = rs.getDate("fecha_nacimiento");
        LocalDate fechaNacimiento = fechaNacimientoSQL != null
                ? fechaNacimientoSQL.toLocalDate() : null;

        User user = new User();
        user.setId(rs.getInt("id"));
        user.setNombre(rs.getString("nombre"));
        user.setApellido(rs.getString("apellido"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setGenero(rs.getString("genero"));
        user.setFechaNacimiento(fechaNacimiento);
        user.setRolId(rs.getInt("rol_id"));
        return user;
    }

    /**
     * Detecta en runtime el nombre de la columna de nombre en la tabla {@code roles}.
     * Garantiza compatibilidad con distintos esquemas de instalación.
     *
     * @return nombre de columna detectado, por defecto {@code "nombre_rol"}
     */
    private String resolveRoleNameColumn() {
        String[] candidates = { "nombre_rol", "nombre", "rol", "name" };
        String sql = "SHOW COLUMNS FROM roles";

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String column = rs.getString("Field");
                for (String candidate : candidates) {
                    if (candidate.equalsIgnoreCase(column)) {
                        return candidate;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "No se pudo resolver columna de roles. Usando 'nombre_rol'.", e);
        }
        return "nombre_rol";
    }
}