package secureauth.ui.dialogs;

import javax.swing.JFrame;

import secureauth.controller.IngresoController;
import secureauth.model.User;

/**
 * Fábrica de diálogos de edición de usuario para inyección manual.
 */
@FunctionalInterface
public interface EditUserDialogFactory {

    /**
     * Abre el diálogo de edición del usuario.
     *
     * @param parent ventana padre
     * @param user usuario a editar
     * @param controller controlador del dashboard
     */
    void show(JFrame parent, User user, IngresoController controller);
}
