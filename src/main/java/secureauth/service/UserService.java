package secureauth.service;

import java.util.List;
import secureauth.dao.UserDAO;
import secureauth.model.User;
import secureauth.model.EmployeeSummary;
import secureauth.repository.UserRepository;
import secureauth.repository.UserRepositoryImpl;

/**
 * Servicio de dominio para la gestión de usuarios.
 *
 * <p>
 * Esta clase centraliza casos de uso de usuarios del dashboard para evitar
 * que la capa Controller dependa de DAO concretos.
 * </p>
 *
 * @author Diego
 * @version 1.0
 */
public class UserService {

    private final UserRepository userRepository;
    private final UserDAO userDAO;

    /**
     * Constructor por defecto.
     */
    public UserService() {
        this(new UserRepositoryImpl());
    }

    /**
     * Constructor para inyección de dependencias.
     *
     * @param userRepository repositorio de usuarios
     */
    public UserService(UserRepository userRepository) {
        this(userRepository, new UserDAO());
    }

    /**
     * Constructor para inyección completa de dependencias.
     *
     * @param userRepository repositorio de usuarios
     * @param userDAO DAO de usuarios para consultas especificas del dashboard
     */
    public UserService(UserRepository userRepository, UserDAO userDAO) {
        this.userRepository = userRepository;
        this.userDAO = userDAO;
    }

    /**
     * Obtiene todos los usuarios.
     *
     * @return lista de usuarios
     */
    public List<User> findAll() {
        return userRepository.findAll();
    }

    /**
     * Busca usuarios por texto.
     *
     * @param text texto a consultar
     * @return usuarios encontrados
     */
    public List<User> search(String text) {
        return userRepository.search(text);
    }

    /**
     * Busca un usuario por id.
     *
     * @param userId identificador del usuario
     * @return usuario o {@code null}
     */
    public User findById(int userId) {
        return userRepository.findById(userId);
    }

    /**
     * Actualiza un usuario.
     *
     * @param user usuario a actualizar
     */
    public void update(User user) {
        userRepository.update(user);
    }

    /**
     * Elimina un usuario por id.
     *
     * @param userId identificador del usuario
     */
    public void delete(int userId) {
        userRepository.delete(userId);
    }

    /**
     * Lista de trabajadores incluyendo nombre del rol.
     *
     * @return filas de trabajadores para tabla de configuración
     */
    public List<EmployeeSummary> findAllWorkersWithRoleName() {
        return userDAO.findAllWithRoleName();
    }

    /**
     * Cuenta usuarios registrados durante el mes actual.
     *
     * @return total de usuarios nuevos del mes
     */
    public int countNewThisMonth() {
        return userDAO.countNewThisMonth();
    }

    public int countActiveUsers() {
        return userDAO.countAllUsers();
    }
}
