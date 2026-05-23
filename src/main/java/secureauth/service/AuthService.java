/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package secureauth.service;

import java.sql.SQLException;
import java.util.Locale;
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

    private final UserRepository userRepository;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    /**
     * Constructor para inyección de dependencias.
     *
     * @param userRepository repositorio de usuarios
     */
    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
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

        validatePasswordStrength(plainPassword);

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
            throw new IllegalArgumentException("Email requerido");
        }

        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password requerido");
        }

        User user = userRepository.findByEmail(normalizedEmail);

        if (user == null) {
            return null;
        }

        boolean isValid = PasswordHasher.verify(password, user.getPassword());

        if (isValid) {
            if (PasswordHasher.needsRehash(user.getPassword())) {
                userRepository.updatePassword(user.getId(), PasswordHasher.hash(password));
            }
            return user;
        }

        return null;
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private void validatePasswordStrength(String password) {
        if (password.length() < 8) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres");
        }
        if (!password.chars().anyMatch(Character::isDigit)) {
            throw new IllegalArgumentException("La contraseña debe incluir al menos un número");
        }
        if (!password.chars().anyMatch(Character::isUpperCase)) {
            throw new IllegalArgumentException("La contraseña debe incluir al menos una mayúscula");
        }
    }
}
