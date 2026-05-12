/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */


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
 * =========================
 * 🔗 DEPENDENCIAS (FLUJO)
 * =========================
 * LoginFrame → AuthController → AuthService → UserDAO → Database
 *
 * Esta clase es la ÚNICA responsable de interactuar con la base de datos.
 *
 * @author Diego
 * @version 2.0
 */
public class UserDAO {

    private static final Logger LOGGER = Logger.getLogger(UserDAO.class.getName());

    /**
     * Data Transfer Object (DTO) liviano diseñado específicamente para el 
     * renderizado de filas en tablas de gestión de personal.
     * 
     * <p>Evita cargar objetos pesados o datos sensibles como contraseñas en la UI.</p>
     */
    public static class WorkerRow {
        private final int id;
        private final String nombre;
        private final String apellido;
        private final String email;
        private final String genero;
        private final String rol;

        public WorkerRow(int id, String nombre, String apellido, String email, String genero, String rol) {
            this.id = id;
            this.nombre = nombre;
            this.apellido = apellido;
            this.email = email;
            this.genero = genero;
            this.rol = rol;
        }

        public int getId() { return id; }
        public String getNombre() { return nombre; }
        public String getApellido() { return apellido; }
        public String getEmail() { return email; }
        public String getGenero() { return genero; }
        public String getRol() { return rol; }
    }

    /**
     * Recupera la totalidad de los usuarios registrados en el sistema.
     * 
     * @return Una {@link List} de objetos {@link User}.
     */
    public List<User> findAll() {

        List<User> lista = new ArrayList<>();

        /**
         * 🔥 SQL:
         * Consulta todos los registros
         */
        String sql = "SELECT id, email, password, nombre, apellido, fecha_nacimiento, genero, rol_id FROM users";

        /**
         * TRY WITH RESOURCES:
         * - Cierra conexión automáticamente
         */
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()) {

            /**
             * 🔥 WHILE:
             * Recorre cada fila del resultado
             */
            while (rs.next()) {

                User user = mapResultSetToUser(rs);
                lista.add(user);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener todos los usuarios", e);
        }

        return lista;
    }

    /**
     * Realiza una búsqueda avanzada filtrando por nombre, apellido o email.
     *
     * @param texto Cadena de búsqueda (query).
     * @return Lista filtrada de usuarios que coinciden con el criterio.
     */
    public List<User> search(String texto) {

        List<User> lista = new ArrayList<>();

        /**
         * 🔥 SQL CON LIKE:
         * Permite búsqueda parcial
         */
        String sql = """
                SELECT id, email, password, nombre, apellido, fecha_nacimiento, genero, rol_id FROM users
                WHERE nombre LIKE ?
                OR apellido LIKE ?
                OR email LIKE ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            /**
             * 🔥 PARAMETROS:
             * Se usa % para coincidencias parciales
             */
            String filtro = "%" + texto + "%";

            stmt.setString(1, filtro);
            stmt.setString(2, filtro);
            stmt.setString(3, filtro);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                User user = mapResultSetToUser(rs);
                lista.add(user);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error en la búsqueda de usuarios", e);
        }

        return lista;
    }

    /**
     * Localiza un usuario mediante su identificador único primario.
     *
     * @param id El ID del usuario en la base de datos.
     * @return El objeto {@link User} encontrado o {@code null} si no existe.
     */
    public User findById(int id) {

        User user = null;
        
        String sql = "SELECT id, email, password, nombre, apellido, fecha_nacimiento, genero, rol_id FROM users WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                user = mapResultSetToUser(rs);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al buscar usuario por ID: " + id, e);
        }

        return user;
    }

    /**
     * Helper encargado de la transformación de datos relacionales (SQL) a 
     * tipos de datos de Java.
     *
     * <p>Maneja la conversión de {@link java.sql.Date} a {@link java.time.LocalDate} 
     * de forma segura para evitar errores de puntero nulo.</p>
     *
     * @param rs ResultSet posicionado en la fila a mapear.
     * @return Objeto {@link User} instanciado y poblado.
     * @throws SQLException Si ocurre un error de lectura en el ResultSet.
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {

            // =========================
            // 1. EXTRACCIÓN DE DATOS
            // =========================
            int id = rs.getInt("id");
            String nombre = rs.getString("nombre");
            String apellido = rs.getString("apellido");
            String email = rs.getString("email");
            String password = rs.getString("password"); 
            String genero = rs.getString("genero");

            Date fechaNacimientoSQL = rs.getDate("fecha_nacimiento");

            // =========================
            // 2. CONVERSIÓN DE FECHA
            // =========================
            LocalDate fechaNacimiento = null;

            /**
             * IF:
             * Verifica si la fecha existe en BD
             * Evita NullPointerException
             */
            if (fechaNacimientoSQL != null) {
                fechaNacimiento = fechaNacimientoSQL.toLocalDate();
            }

            // =========================
            // 3. CREACIÓN DEL OBJETO
            // =========================
            User user = new User();

            user.setId(id);
            user.setNombre(nombre);
            user.setApellido(apellido);
            user.setEmail(email);
            user.setPassword(password);
            user.setGenero(genero);
            user.setFechaNacimiento(fechaNacimiento);
            user.setRolId(rs.getInt("rol_id"));

            return user;
    }
    
    /**
     * Persiste un nuevo registro de usuario en la tabla {@code users}.
     *
     * @param user Objeto con los datos del nuevo usuario.
     * @return {@code true} si la operación afectó al menos una fila en la DB.
     * @throws NullPointerException si el usuario es nulo.
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

            /**
             * 🔥 CONVERSIÓN LocalDate → SQL Date
             */
            ps.setDate(6, user.getFechaNacimiento() != null 
                    ? Date.valueOf(user.getFechaNacimiento()) 
                    : null);
            ps.setInt(7, user.getRolId()); // Asegurado RolId

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al insertar usuario", e);
            return false;
        }
}




    /**
     * Recupera un usuario basándose estrictamente en su correo electrónico.
     * 
     * @param email Email a consultar.
     * @return Objeto {@link User} o {@code null} si no se encuentra el correo.
     */
    public User findByEmail(String email) {

        String sql = "SELECT id, email, password, nombre, apellido, fecha_nacimiento, genero, rol_id FROM users WHERE email = ?";

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            /**
             * 🔥 NORMALIZACIÓN DE DATOS
             */
            ps.setString(1, email.trim());

            ResultSet rs = ps.executeQuery();

            /**
             * IF:
             * - true → existe usuario
             * - false → no existe
             */
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al buscar usuario por email", e);
        }

        return null;
    }

    /**
     * Ejecuta un JOIN entre {@code users} y {@code roles} para obtener una lista 
     * de trabajadores con el nombre legible de su cargo.
     *
     * @param query Filtro opcional de texto para búsqueda.
     * @return Lista de {@link WorkerRow} para visualización en tablas.
     */
    public List<WorkerRow> findAllWithRoleName(String query) {
        List<WorkerRow> workers = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
                SELECT u.id, u.nombre, u.apellido, u.email, u.genero,
                    COALESCE(r.nombre, 'Sin rol') AS rol
                FROM users u
                LEFT JOIN roles r ON r.id = u.rol_id
                """);

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

            ResultSet rs = stmt.executeQuery();
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
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al listar trabajadores con rol", e);
        }

        return workers;
    }

    public List<WorkerRow> findAllWithRoleName() {
        return findAllWithRoleName(null);
    }

    /**
     * Actualiza de forma atómica la contraseña de un usuario.
     *
     * @param userId ID del usuario.
     * @param hashedPassword El nuevo hash seguro (ej: BCrypt).
     * @return true si se actualizó el registro
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
     * Remueve físicamente un registro de usuario de la base de datos.
     *
     * @param id ID del usuario a dar de baja.
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
     * Sincroniza los cambios de un objeto {@link User} existente con su 
     * fila correspondiente en la base de datos.
     *
     * @param user Objeto con los datos actualizados.
     * @throws NullPointerException si el usuario es nulo.
     */
    public void update(User user) {
        Objects.requireNonNull(user, "El usuario no puede ser nulo para la actualización");

        String sql = "UPDATE users SET nombre = ?, apellido = ?, email = ?, password = ?, genero = ?, fecha_nacimiento = ?, rol_id = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getNombre());
            ps.setString(2, user.getApellido());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getPassword());
            ps.setString(5, user.getGenero());
            ps.setDate(6, user.getFechaNacimiento() != null 
                    ? Date.valueOf(user.getFechaNacimiento()) 
                    : null);
            ps.setInt(7, user.getRolId()); // Asegurado RolId
            ps.setInt(8, user.getId());

            ps.executeUpdate();

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al actualizar usuario", e);
        }

    }
}
