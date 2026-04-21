package secureauth.repository;

import java.util.List;
import secureauth.model.User;

/**
 * Contrato de acceso a datos de usuarios para la capa de servicio.
 *
 * <p>Este contrato desacopla la lógica de negocio de la implementación concreta
 * de persistencia.</p>
 *
 * @author Diego
 * @version 1.0
 */
public interface UserRepository {

    /**
     * Busca un usuario por su correo.
     *
     * @param email correo a consultar
     * @return usuario encontrado o {@code null} si no existe
     */
    User findByEmail(String email);

    /**
     * Inserta un nuevo usuario.
     *
     * @param user usuario a registrar
     * @return {@code true} si se insertó correctamente
     */
    boolean insert(User user);

    /**
     * Actualiza la contraseña de un usuario.
     *
     * @param userId identificador del usuario
     * @param hashedPassword contraseña cifrada/hash
     * @return {@code true} si se actualizó correctamente
     */
    boolean updatePassword(int userId, String hashedPassword);

    /**
     * Obtiene todos los usuarios registrados.
     *
     * @return lista de usuarios
     */
    List<User> findAll();

    /**
     * Busca usuarios por texto libre.
     *
     * @param text texto a buscar en nombre, apellido o email
     * @return lista de usuarios que coinciden
     */
    List<User> search(String text);

    /**
     * Busca un usuario por identificador.
     *
     * @param userId identificador del usuario
     * @return usuario encontrado o {@code null}
     */
    User findById(int userId);

    /**
     * Actualiza datos de un usuario.
     *
     * @param user usuario con cambios
     */
    void update(User user);

    /**
     * Elimina un usuario por su identificador.
     *
     * @param userId identificador del usuario
     */
    void delete(int userId);
}
