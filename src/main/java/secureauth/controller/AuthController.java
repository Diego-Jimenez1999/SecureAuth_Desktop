package secureauth.controller;

import java.sql.SQLException;
import java.util.Objects;

import secureauth.model.User;
import secureauth.service.AuthService;

/**
 * Controlador encargado de manejar la comunicación entre la UI
 * y la lógica de negocio ({@link AuthService}).
 *
 * <p>Pertenece a la capa Controller en la arquitectura MVC.</p>
 *
 * <pre>
 * LoginFrame → AuthController → AuthService → UserRepository → UserDAO → DB
 * </pre>
 *
 * @author Diego
 * @version 1.1 — Corrección DI: usa el AuthService inyectado en lugar de crear uno nuevo.
 */
public class AuthController {

    private final AuthService authService;

    /**
     * Constructor con inyección de dependencias.
     *
     * @param authService servicio de autenticación ya construido en el bootstrap
     * @throws NullPointerException si {@code authService} es null
     */
    public AuthController(AuthService authService) {
        // BUG CORREGIDO: la versión anterior ignoraba el parámetro e instanciaba
        // new AuthService() internamente, rompiendo toda la cadena de DI configurada
        // en MainApp y creando un segundo UserRepositoryImpl sin contexto.
        this.authService = Objects.requireNonNull(authService, "AuthService requerido");
    }

    /**
     * Delega el proceso de login al servicio de autenticación.
     *
     * @param email    correo del usuario (se normaliza en AuthService)
     * @param password contraseña en texto plano
     * @return {@link User} autenticado, o {@code null} si las credenciales no coinciden
     * @throws SQLException             si ocurre un error de base de datos
     * @throws IllegalArgumentException si email o password están vacíos
     */
    public User login(String email, String password) throws SQLException {
        return authService.login(email, password);
    }

    /**
     * Delega el registro de un nuevo usuario al servicio de autenticación.
     *
     * @param user objeto {@link User} con los datos del nuevo usuario
     * @return {@code true} si el registro fue exitoso
     * @throws SQLException             si ocurre un error de base de datos
     * @throws IllegalArgumentException si algún campo obligatorio falta o es inválido
     */
    public boolean register(User user) throws SQLException {
        return authService.register(user);
    }
}