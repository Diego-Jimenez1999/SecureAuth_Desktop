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

    /**
     * =========================
     * OBTENER TODOS LOS USUARIOS
     * =========================
     * 
     * @return Lista de usuarios
     */
    public List<User> findAll() {

        List<User> lista = new ArrayList<>();

        /**
         * 🔥 SQL:
         * Consulta todos los registros
         */
        String sql = "SELECT * FROM users";

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

            e.printStackTrace();
        }

        return lista;
    }

    /**
     * =========================
     * BUSCAR USUARIOS
     * =========================
     *
     * @param texto texto a buscar
     * @return lista filtrada
     */
    public List<User> search(String texto) {

        List<User> lista = new ArrayList<>();

        /**
         * 🔥 SQL CON LIKE:
         * Permite búsqueda parcial
         */
        String sql = """
                SELECT * FROM users
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
            e.printStackTrace();
        }

        return lista;
    }

    /**
     * =========================
     * OBTENER USUARIO POR ID
     * =========================
     *
     * Busca un usuario específico por su ID
     *
     * @param id identificador del usuario
     * @return objeto User o null si no existe
     */
    public User findById(int id) {

        User user = null;
        
        String sql = "SELECT * FROM users WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                user = mapResultSetToUser(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return user;
    }

        /**
     * =========================
     * MAPEAR RESULTSET → USER
     * =========================
     *
     * Convierte una fila de la base de datos en un objeto User.
     *
     * 🔥 RESPONSABILIDAD:
     * - Transformar datos SQL → Objeto Java
     * - Centralizar el mapeo (evita duplicación)
     *
     * @param rs ResultSet con la fila actual
     * @return objeto User completamente construido
     * @throws SQLException si ocurre error al leer datos
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

            return user;
    }
    
    
        /**
     * Inserta un nuevo usuario en la base de datos.
     *
     * @param user objeto usuario
     * @return true si se insertó correctamente
     */
    public boolean insert(User user) {

        String sql = "INSERT INTO users (nombre, apellido, email, password, genero, fecha_nacimiento) VALUES (?, ?, ?, ?, ?, ?)";

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
            ps.setDate(6, Date.valueOf(user.getFechaNacimiento()));

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
}




    /**
     * Busca un usuario por email.
     * 
     * @param email correo del usuario
     * @return User o null si no existe
     */
    public User findByEmail(String email) {

        String sql = "SELECT * FROM users WHERE email = ?";

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

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Actualiza únicamente la contraseña de un usuario.
     *
     * @param userId identificador del usuario
     * @param hashedPassword contraseña hash
     * @return true si se actualizó el registro
     */
    public boolean updatePassword(int userId, String hashedPassword) {
        String sql = "UPDATE users SET password = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hashedPassword);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * Elimina un usuario por su ID.
     *
     * @param id identificador del usuario a eliminar
     */
    public void delete(int id) {

        String sql = "DELETE FROM users WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
    }
    
}
    /**
     * Actualiza un usuario existente.
     *
     * @param user objeto usuario
     */
    public void update(User user) {

        String sql = "UPDATE users SET nombre = ?, apellido = ?, email = ?, password = ?, genero = ?, fecha_nacimiento = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getNombre());
            ps.setString(2, user.getApellido());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getPassword());
            ps.setString(5, user.getGenero());
            ps.setDate(6, Date.valueOf(user.getFechaNacimiento()));
            ps.setInt(7, user.getId());

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
