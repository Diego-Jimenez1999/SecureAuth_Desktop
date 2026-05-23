package secureauth.controller;

import java.util.List;
import javax.swing.table.DefaultTableModel;
import secureauth.model.User;
import secureauth.service.UserService;
import secureauth.ui.frames.IngresoFrame;
import secureauth.ui.dialogs.EditUserDialogFactory;

/**
 * Controlador del dashboard de usuarios.
 *
 * <p>
 * Este controlador queda desacoplado del armado de dependencias:
 * recibe servicios y callbacks desde el bootstrap de la aplicación.
 * </p>
 *
 * @author Diego
 * @version 2.0
 */
public class IngresoController {

    private final UserService userService;
    private final Runnable onLogout;
    private final EditUserDialogFactory editUserDialogFactory;

    private IngresoFrame view;

    /**
     * Constructor principal para inyección de dependencias.
     *
     * @param userService servicio de usuarios
     * @param onLogout acción a ejecutar al cerrar sesión
     */
    public IngresoController(UserService userService, Runnable onLogout, EditUserDialogFactory editUserDialogFactory) {
        this.userService = userService;
        this.onLogout = onLogout;
        this.editUserDialogFactory = editUserDialogFactory;
    }

    /**
     * Enlaza la vista y usuario de sesión con este controlador.
     *
     * @param view vista del dashboard
     * @param currentUser usuario autenticado
     */
    public void bindView(IngresoFrame view, User currentUser) {
        this.view = view;
    }

    /**
     * Cierra sesión y retorna al flujo de login.
     */
    public void logout() {
        if (view != null) {
            view.dispose();
        }
        if (onLogout != null) {
            onLogout.run();
        }
    }

    /**
     * Carga usuarios en la tabla principal.
     */
    public void cargarUsuarios() {
        if (view == null) {
            return;
        }

        List<User> lista = userService.findAll();
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
     * Filtra usuarios por texto.
     */
    public void buscarUsuarios() {
        if (view == null) {
            return;
        }

        String texto = view.getTextoBusqueda();
        if (texto == null || texto.isEmpty()) {
            cargarUsuarios();
            return;
        }

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
     * Abre el formulario de edición de un usuario.
     *
     * @param userId identificador del usuario
     */
    public void editarUsuario(int userId) {
        if (view == null) {
            return;
        }

        User user = userService.findById(userId);
        if (user != null) {
            editUserDialogFactory.show(view, user, this);
        }
    }

    /**
     * Actualiza un usuario y refresca la tabla.
     *
     * @param user usuario actualizado
     */
    public void actualizarUsuario(User user) {
        userService.update(user);
    }

    /**
     * Elimina un usuario y refresca la tabla.
     *
     * @param userId identificador del usuario
     */
    public void eliminarUsuario(int userId) {
        userService.delete(userId);
    }
}
