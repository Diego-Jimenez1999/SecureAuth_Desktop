/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
 
package secureauth.service;
 
import java.sql.SQLException;
import java.util.Locale;
import java.util.logging.Logger;
import java.util.regex.Pattern;
 
import secureauth.model.User;
import secureauth.repository.UserRepository;
import secureauth.security.PasswordHasher;
 
/**
 * Servicio encargado de la lógica de autenticación del sistema.
 *
 * <p>
 * Esta clase pertenece a la capa de servicio (Service) y actúa como intermediario
 * entre la interfaz de usuario (UI) y el acceso a datos (DAO).
 * </p>
 *
 * <h2>Responsabilidades</h2>
 * <ul>
 *     <li>Registrar usuarios</li>
 *     <li>Validar login</li>
 *     <li>Aplicar reglas de negocio</li>
 * </ul>
 *
 * @author Diego
 * @version 1.0
 */
public class AuthService {
 
    private static final Logger LOGGER = Logger.getLogger(AuthService.class.getName());
    private final UserRepository userRepository;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
 
    /**
     * Constructor con inyección de dependencias.
     *
     * <p>Use siempre este constructor. El repositorio se construye en {@code MainApp}
     * y se pasa hacia abajo por toda la cadena, evitando instancias sueltas.</p>
     *
     * @param userRepository repositorio de usuarios
     * @throws NullPointerException si {@code userRepository} es null
     */
    public AuthService(UserRepository userRepository) {
        this.userRepository = java.util.Objects.requireNonNull(userRepository, "UserRepository requerido");
    }
 
    /**
     * Registra un nuevo usuario en el sistema.
     *
     * @param user objeto {@link User} con los datos del usuario
     * @return true si el registro fue exitoso
     * @throws SQLException si ocurre un error en la BD
     */
    public boolean register(User user) throws SQLException {
        if (user == null) {
            throw new IllegalArgumentException("Usuario requerido");
        }
 
        String email = normalizeEmail(user.getEmail());
        String plainPassword = user.getPassword();
 
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Email requerido");
        }
 
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Email inválido");
        }
 
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password requerido");
        }
 
        if (user.getNombre() == null || user.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("Nombre requerido");
        }
        if (user.getApellido() == null || user.getApellido().trim().isEmpty()) {
            throw new IllegalArgumentException("Apellido requerido");
        }
        if (user.getFechaNacimiento() == null) {
            throw new IllegalArgumentException("Fecha de nacimiento requerida");
        }
        
        // Asignar rol por defecto (Recepcionista = 3) si no viene especificado
        if (user.getRolId() <= 0) {
            user.setRolId(3);
        }
 
        if (!PasswordHasher.isStrongPassword(plainPassword)) {
            throw new IllegalArgumentException(PasswordHasher.getPolicyMessage());
        }
 
        User existingUser = userRepository.findByEmail(email);
 
        if (existingUser != null) {
            throw new IllegalArgumentException("El usuario ya existe");
        }
 
        String hashedPassword = PasswordHasher.hash(plainPassword);
        user.setEmail(email);
        user.setPassword(hashedPassword);
 
        return userRepository.insert(user);
    }
 
    /**
     * Valida el login de un usuario.
     *
     * @param email correo del usuario
     * @param password contraseña en texto plano
     * @return objeto {@link User} si el login es correcto, null si falla
     * @throws SQLException si ocurre error en BD
     */
    public User login(String email, String password) throws SQLException {
        String normalizedEmail = normalizeEmail(email);
 
        if (normalizedEmail == null || normalizedEmail.isEmpty()) {
            throw new IllegalArgumentException("El correo electrónico es obligatorio.");
        }
 
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("La contraseña es obligatoria.");
        }
 
        User user = userRepository.findByEmail(normalizedEmail);
 
        if (user == null) {
            LOGGER.warning("Intento de login fallido: Usuario no encontrado o inactivo -> " + normalizedEmail);
            return null;
        }
 
        boolean isValid = PasswordHasher.verify(password, user.getPassword());
 
        if (isValid) {
            LOGGER.info("Autenticación exitosa para: " + normalizedEmail);
            // Migración automática: Si el password era texto plano o hash débil, actualizar a BCrypt
            if (!PasswordHasher.isBcryptHash(user.getPassword())) {
                LOGGER.info("Migrando contraseña de usuario " + user.getId() + " a BCrypt.");
                userRepository.updatePassword(user.getId(), PasswordHasher.hash(password));
            }
            return user;
        }
 
        LOGGER.warning("Intento de login fallido: Contraseña incorrecta para -> " + normalizedEmail);
        return null;
    }
 
    private String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }
}