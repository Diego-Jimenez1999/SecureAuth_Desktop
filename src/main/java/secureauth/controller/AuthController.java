/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */


package secureauth.controller;

import java.sql.SQLException;

import secureauth.model.User;
import secureauth.service.AuthService;

/**
 * Controlador encargado de manejar la comunicación entre la UI
 * y la lógica de negocio (AuthService).
 *
 * <p>
 * Esta clase pertenece a la capa Controller en la arquitectura MVC.
 * </p>
 *
 * @author Diego
 * @version 1.0
 */
public class AuthController {

    private final AuthService authService;

    /**
     * Constructor del controlador.
     */
    public AuthController(AuthService authService1) {
        this.authService = new AuthService();
    }

    /**
     * Maneja el proceso de login desde la UI.
     *
     * @param email correo del usuario
     * @param password contraseña en texto plano
     * @return {@link User} si el login es exitoso, null si falla
     * @throws SQLException error en base de datos
     */
    public User login(String email, String password) throws SQLException {

        return authService.login(email, password);
    }

    /**
     * Maneja el registro de usuarios.
     *
     * @param user objeto {@link User}
     * @return true si se registra correctamente
     * @throws SQLException error en BD
     */
    public boolean register(User user) throws SQLException {

        return authService.register(user);
    }
}