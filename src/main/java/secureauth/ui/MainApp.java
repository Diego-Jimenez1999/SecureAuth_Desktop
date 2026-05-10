package secureauth.ui;

import javax.swing.SwingUtilities;

import secureauth.controller.AuthController;
import secureauth.controller.IngresoController;
import secureauth.model.User;
import secureauth.repository.UserRepository;
import secureauth.repository.UserRepositoryImpl;
import secureauth.service.AuthService;
import secureauth.service.UserService;
import secureauth.ui.dialogs.EditUserDialogFactory;
import secureauth.ui.dialogs.SubServiceDialog;
import secureauth.ui.dialogs.SubServiceSelector;
import secureauth.ui.frames.IngresoFrame;
import secureauth.ui.frames.LoginFrame;

/**
 * Punto de entrada oficial de SecureAuth Desktop.
 *
 * <p>
 * Esta clase aplica un bootstrap limpio:
 * </p>
 * <ul>
 *     <li>Construye Repository, Service y Controller una sola vez.</li>
 *     <li>Inyecta dependencias en las vistas en lugar de usar {@code new} dentro de UI.</li>
 *     <li>Centraliza navegación entre pantallas (login y dashboard).</li>
 * </ul>
 *
 * @author Diego Jimenez
 * @version 1.0
 */
public final class MainApp {

    private final UserRepository userRepository; // Repositorio compartido para toda la app
    private final AuthService authService; // Servicio de autenticación
    private final UserService userService; // Servicio de gestión de usuarios
    private final AuthController authController; // Controlador de autenticación
    private final EditUserDialogFactory editUserDialogFactory;
    private final SubServiceSelector subServiceSelector;

    /**
     * Constructor del bootstrap principal.
     *
     * <p>
     * Aquí se arma el grafo de dependencias una sola vez para toda la app.
     * </p>
     */
    public MainApp() {

        this.userRepository = new UserRepositoryImpl();// Implementación concreta del repositorio de usuarios
        this.authService = new AuthService(userRepository);// Servicio de autenticación que depende del repositorio
        this.userService = new UserService(userRepository);// Servicio de gestión de usuarios que también depende del repositorio
        this.authController = new AuthController(authService);// Controlador de autenticación que depende del servicio de autenticación
        
        this.editUserDialogFactory = (parent, user, controller) -> new secureauth.ui.frames.EditUserFrame(parent, user,
                controller).setVisible(true);
        this.subServiceSelector = (parent, serviceName, subServices) -> {
            SubServiceDialog dialog = new SubServiceDialog(parent, serviceName, subServices);
            dialog.setVisible(true);
            return dialog.getSelectedItem();
        };
    }

    /**
     * Método principal de ejecución.
     *
     * @param args argumentos de línea de comandos
     */
    public static void main(String[] args) {
        // Inicia la aplicación en el hilo de eventos de Swing
        SwingUtilities.invokeLater(() -> new MainApp().showLoginFrame());
    }

    /**
     * Muestra la pantalla de login inyectando controlador y callback de éxito.
     */
    private void showLoginFrame() {
        LoginFrame loginFrame = new LoginFrame(authController, this::showDashboard);
        loginFrame.setVisible(true);
    }

    /**
     * Muestra dashboard para el usuario autenticado.
     *
     * @param user usuario autenticado
     */
    private void showDashboard(User user) {
        IngresoController ingresoController = new IngresoController(userService, this::showLoginFrame,
                editUserDialogFactory);
        IngresoFrame ingresoFrame = new IngresoFrame(ingresoController, user, subServiceSelector);
        ingresoFrame.setVisible(true);
    }
}
