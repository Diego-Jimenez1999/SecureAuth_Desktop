package secureauth.repository;

import java.util.List;
import secureauth.dao.UserDAO;
import secureauth.model.User;

/**
 * Implementación de {@link UserRepository} para MySQL usando {@link UserDAO}.
 *
 * @author Diego
 * @version 1.0
 */
public class UserRepositoryImpl implements UserRepository {

    private final UserDAO userDAO;

    /**
     * Crea el repositorio con su dependencia DAO.
     */
    public UserRepositoryImpl() {
        this(new UserDAO());
    }

    /**
     * Crea el repositorio con el DAO inyectado.
     *
     * @param userDAO DAO de usuarios
     */
    public UserRepositoryImpl(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    public User findByEmail(String email) {
        return userDAO.findByEmail(email);
    }

    @Override
    public boolean insert(User user) {
        return userDAO.insert(user);
    }

    @Override
    public boolean updatePassword(int userId, String hashedPassword) {
        return userDAO.updatePassword(userId, hashedPassword);
    }

    @Override
    public List<User> findAll() {
        return userDAO.findAll();
    }

    @Override
    public List<User> search(String text) {
        return userDAO.search(text);
    }

    @Override
    public User findById(int userId) {
        return userDAO.findById(userId);
    }

    @Override
    public void update(User user) {
        userDAO.update(user);
    }

    @Override
    public void delete(int userId) {
        userDAO.delete(userId);
    }
}
