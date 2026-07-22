package secureauth.ui.config;

import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import secureauth.config.AppContext;
import secureauth.controller.IngresoController;
import secureauth.model.User;
import secureauth.service.enterprise.SessionManager;
import secureauth.ui.dialogs.EditUserDialogFactory;
import secureauth.ui.dialogs.SubServiceDialog;
import secureauth.ui.dialogs.SubServiceSelector;
import secureauth.ui.frames.IngresoFrame;
import secureauth.ui.frames.LoginFrame;

/**
 * Controlador centralizado de ventanas (patrón Singleton/Mediator) — WindowManager.
 *
 * <p>Responsable absoluto del ciclo de vida de los contenedores gráficos principales
 * (JFrames, JDialogs, JPanels), la transición entre el flujo de autenticación y
 * el panel principal del ERP, y la liberación de recursos en memoria.</p>
 *
 * @author Jules
 * @version 1.0
 */
public final class WindowManager {

    private static final WindowManager INSTANCE = new WindowManager();

    private final AppContext appContext;
    private final EditUserDialogFactory editUserDialogFactory;
    private final SubServiceSelector subServiceSelector;

    private LoginFrame loginFrame;
    private IngresoFrame ingresoFrame;

    /**
     * Constructor privado de inicialización del WindowManager.
     * Carga el contexto centralizado de dependencias (AppContext).
     */
    private WindowManager() {
        this.appContext = new AppContext();
        this.appContext.initialize();

        // Inicialización de fábricas y selectores Swing requeridos por las ventanas
        this.editUserDialogFactory = (parent, user, controller) ->
                new secureauth.ui.frames.EditUserFrame(parent, user, controller)
                        .setVisible(true);

        this.subServiceSelector = (parent, serviceName, subServices) -> {
            SubServiceDialog dialog = new SubServiceDialog(parent, serviceName, subServices);
            dialog.setVisible(true);
            return dialog.getSelectedItem();
        };
    }

    /**
     * Retorna la instancia Singleton única del WindowManager.
     *
     * @return instancia de WindowManager
     */
    public static WindowManager getInstance() {
        return INSTANCE;
    }

    /**
     * Obtiene el contexto de dependencias de la aplicación.
     *
     * @return instancia de AppContext
     */
    public AppContext getAppContext() {
        return appContext;
    }

    /**
     * Abre y muestra la ventana de inicio de sesión (LoginFrame).
     * Si la ventana principal del Dashboard ya está instanciada, la libera y cierra.
     */
    public void showLogin() {
        SwingUtilities.invokeLater(() -> {
            if (ingresoFrame != null) {
                ingresoFrame.setVisible(false);
                ingresoFrame.dispose();
                ingresoFrame = null;
            }

            // Limpia cualquier residuo de sesión previa
            SessionManager.getInstance().clearSession();

            if (loginFrame == null) {
                loginFrame = new LoginFrame(
                        appContext.getAuthController(),
                        user -> showDashboard(user)
                );
            }

            loginFrame.setLocationRelativeTo(null);
            loginFrame.setVisible(true);
        });
    }

    /**
     * Abre e inicia el Dashboard principal del ERP completamente maximizado.
     * Si la pantalla de Login está activa, se cierra y se liberan sus recursos.
     * Inicializa la sesión del usuario en {@link SessionManager}.
     *
     * @param user usuario autenticado que accede al sistema
     */
    public void showDashboard(User user) {
        SwingUtilities.invokeLater(() -> {
            // Inicializar sesión única tras autenticación exitosa
            SessionManager.getInstance().initializeSession(user);

            if (loginFrame != null) {
                loginFrame.setVisible(false);
                loginFrame.dispose();
                loginFrame = null;
            }

            if (ingresoFrame != null) {
                ingresoFrame.setVisible(false);
                ingresoFrame.dispose();
                ingresoFrame = null;
            }

            IngresoController ingresoController = new IngresoController(
                    appContext.getUserService(),
                    this::showLogin,
                    editUserDialogFactory
            );

            ingresoFrame = new IngresoFrame(
                    ingresoController,
                    user,
                    subServiceSelector,
                    appContext
            );

            // Requerimiento: Pantalla principal completamente maximizada, adaptada responsivamente
            ingresoFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            ingresoFrame.setVisible(true);
        });
    }

    /**
     * Retorna la referencia activa de la ventana del Dashboard.
     *
     * @return instancia de IngresoFrame, o {@code null} si no está activa
     */
    public IngresoFrame getIngresoFrame() {
        return ingresoFrame;
    }
}
