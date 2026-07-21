package secureauth.ui;

import java.awt.Dimension;

import javax.swing.SwingUtilities;

import secureauth.config.AppContext;
import secureauth.controller.IngresoController;
import secureauth.model.User;
import secureauth.ui.dialogs.EditUserDialogFactory;
import secureauth.ui.dialogs.SubServiceDialog;
import secureauth.ui.dialogs.SubServiceSelector;
import secureauth.ui.frames.IngresoFrame;
import secureauth.ui.frames.LoginFrame;

/**
 * Punto de entrada oficial de SecureAuth Desktop.4
 *
 * Bootstrap centralizado:
 * - Inicializa contexto de aplicación
 * - Inyecta dependencias (controllers, services)
 * - Controla navegación entre Login y Dashboard
 */
public final class MainApp {

    private final AppContext appContext;
    private final EditUserDialogFactory editUserDialogFactory;
    private final SubServiceSelector subServiceSelector;

    public MainApp() {

        this.appContext = new AppContext();
        this.appContext.initialize();

        // Factory de edición de usuario
        this.editUserDialogFactory = (parent, user, controller) ->
                new secureauth.ui.frames.EditUserFrame(parent, user, controller)
                        .setVisible(true);

        // Selector de subservicios
        this.subServiceSelector = (parent, serviceName, subServices) -> {
            SubServiceDialog dialog = new SubServiceDialog(parent, serviceName, subServices);
            dialog.setVisible(true);
            return dialog.getSelectedItem();
        };
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainApp().showLoginFrame());
    }

    /**
     * Muestra pantalla de login.
     */
    private void showLoginFrame() {

        final LoginFrame[] holder = new LoginFrame[1];

        LoginFrame loginFrame = new LoginFrame(
                appContext.getAuthController(),
                user -> showDashboard(user, loginFrameSize(holder[0]))
        );

        holder[0] = loginFrame;
        loginFrame.setVisible(true);
    }

    private Dimension loginFrameSize(LoginFrame loginFrame) {
        return loginFrame != null ? loginFrame.getSize() : null;
    }

    /**
     * Abre dashboard principal después del login.
     */
    private void showDashboard(User user, Dimension preferredSize) {

        IngresoController ingresoController = new IngresoController(
                appContext.getUserService(),
                this::showLoginFrame,
                editUserDialogFactory
        );

        IngresoFrame ingresoFrame = new IngresoFrame(
                ingresoController,
                user,
                subServiceSelector,
                appContext
        );

        if (preferredSize != null &&
                preferredSize.width > 0 &&
                preferredSize.height > 0) {

            ingresoFrame.setSize(preferredSize);
            ingresoFrame.setLocationRelativeTo(null);
        }

        ingresoFrame.setVisible(true);
    }
}