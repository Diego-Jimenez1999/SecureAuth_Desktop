/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package secureauth.controller;

import java.util.List;

import javax.swing.table.DefaultTableModel;

import secureauth.model.User;
import secureauth.service.UserService;
import secureauth.ui.IngresoFrame;

/**
 * Controlador del IngresoFrame
 *
 * Se encarga de:
 * - Cargar datos en la tabla
 * - Gestionar búsquedas
 * - Conectar UI con Service
 *
 * Forma parte del patrón MVC
 *
 * @author Diego
 * @version 1.0
 */
public class IngresoController {

    private final IngresoFrame view;
    private final UserService userService;
    private User currentUser; // Usuario actualmente logueado

    /**
     * Constructor del controlador
     *
     * @param view vista principal
     */
    public IngresoController(IngresoFrame view) {

        /**
         * 🔥 DEPENDENCIAS
         * Se inyecta la vista y el servicio
         */
        this.view = view;
        this.userService = new UserService();
        this.currentUser = new User();

    }

    /**
     * Constructor del controlador
     *
     * @param view vista principal
     * @param currentUser usuario actualmente logueado
     */
    public IngresoController(IngresoFrame view, User currentUser) {

        /**
         * 🔥 DEPENDENCIAS
         * Se inyecta la vista y el servicio
         */
        this.view = view;
        this.userService = new UserService();
        this.currentUser = currentUser;

    }



    /**
        * =========================
        * CERRAR SESIÓN
        * =========================
        *
        * Responsabilidades:
        * - Limpiar sesión actual
        * - Liberar recursos
        * - Preparar salida segura
    */
    public void logout() {

        System.out.println("Cerrando sesión...");

        try{

             currentUser=null; // Limpiar usuario actual

            // Aquí podrías agregar lógica adicional, como cerrar conexiones, limpiar caché, etc.
            System.out.println("Sesión cerrada exitosamente.");

            new secureauth.ui.LoginFrame().setVisible(true);

             view.dispose(); // Cerrar ventana actual

        }catch(Exception e){
            System.err.println("Error al cerrar sesión: " + e.getMessage());

    }
    }


    /**
     * =========================
     * CARGAR USUARIOS EN TABLA
     * =========================
     */
    public void cargarUsuarios() {

        /**
         * 🔥 OBTENER DATOS
         */
        List<User> lista = userService.findAll();

        /**
         * 🔥 MODELO DE TABLA
         */
        DefaultTableModel model = (DefaultTableModel) view.getTable().getModel();

        /**
         * 🔥 LIMPIAR TABLA
         */
        model.setRowCount(0);

        /**
         * 🔥 FOR:
         * Recorre todos los usuarios
         */
        for (User u : lista) {

            model.addRow(new Object[]{
                    u.getId(),                                  // ID
                    u.getNombre() + " " + u.getApellido(),     // NOMBRE COMPLETO
                    u.getEmail(),                               // EMAIL
                    u.getGenero(),                              // GENERO
                    "Editar | Eliminar"                         // ACCION
            });
        }
    }

    /**
     * =========================
     * BUSCAR USUARIOS
     * =========================
     */
    public void buscarUsuarios() {

        /**
         * 🔥 TEXTO DE BÚSQUEDA
         */
        String texto = view.getTextoBusqueda();

        /**
         * 🔥 VALIDACIÓN
         */
        if (texto == null || texto.isEmpty()) {
            cargarUsuarios();
            return;
        }

        /**
         * 🔥 CONSULTA FILTRADA
         */
        List<User> lista = userService.search(texto);

        DefaultTableModel model = (DefaultTableModel) view.getTable().getModel();
        model.setRowCount(0);

        for (User u : lista) {
            model.addRow(new Object[]{
                    u.getId(),
                    u.getNombre() + " " + u.getApellido(),
                    u.getEmail(),
                    u.getGenero(),
                    "Editar | Eliminar"
            });
        }
    }

    /**
     * Edita un usuario por su ID.
     *
     * @param userId identificador del usuario a editar
     */
    public void editarUsuario(int userId) {
        User user = userService.findById(userId);
        if (user != null) {
            new secureauth.ui.EditUserFrame(view, user, this).setVisible(true);
        }
    }
    

    /**
     * Actualiza un usuario existente.
     *
     * @param user usuario con datos actualizados
     */
    public void actualizarUsuario(User user) {
        userService.update(user);
        cargarUsuarios();
    }



    
    /**
     * Elimina un usuario por su ID.
     *
     * @param userId identificador del usuario a eliminar
     */
    public void eliminarUsuario(int userId) {
        userService.delete(userId);
        cargarUsuarios();
    }
}
