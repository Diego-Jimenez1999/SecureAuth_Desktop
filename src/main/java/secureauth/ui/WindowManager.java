package secureauth.ui;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import secureauth.config.AppContext;
import secureauth.controller.IngresoController;
import secureauth.model.User;
import secureauth.security.SessionManager;
import secureauth.ui.dialogs.SubServiceSelector;
import secureauth.ui.frames.IngresoFrame;
import secureauth.ui.frames.LoginFrame;

/**
 * Coordinador global de ventanas y navegación Swing (Singleton).
 *
 * Gestiona el ciclo de vida de los frames principales (LoginFrame, IngresoFrame),
 * maximiza IngresoFrame tras un inicio de sesión exitoso, rutea cambios de módulo
 * mediante showModule, y libera recursos de memoria cerrando ventanas previas.
 */
public final class WindowManager {

    private static final Logger LOGGER = Logger.getLogger(WindowManager.class.getName());
    private static final WindowManager INSTANCE = new WindowManager();

    private final Map<String, JFrame> activeFrames = new HashMap<>();
    private AppContext appContext;
    private User loggedUser;

    private WindowManager() {}

    public static WindowManager getInstance() {
        return INSTANCE;
    }

    public void setAppContext(AppContext appContext) {
        this.appContext = appContext;
    }

    /**
     * Muestra la pantalla de inicio de sesión y limpia frames previos.
     */
    public void showLoginFrame() {
        SwingUtilities.invokeLater(() -> {
            disposeFrame("main");

            LoginFrame loginFrame = new LoginFrame(
                appContext.getAuthController(),
                user -> showMainFrame(user)
            );
            activeFrames.put("login", loginFrame);

            loginFrame.setLocationRelativeTo(null);
            loginFrame.setVisible(true);
            LOGGER.info("LoginFrame mostrado exitosamente desde WindowManager.");
        });
    }

    /**
     * Muestra el panel principal o dashboard (IngresoFrame) tras un login exitoso,
     * maximizándolo en pantalla y cerrando la ventana de login.
     *
     * @param user usuario autenticado
     */
    public void showMainFrame(User user) {
        this.loggedUser = user;
        // Inicializar permisos y módulos habilitados en la sesión del usuario
        SessionManager.getInstance().initializeSession(user);

        SwingUtilities.invokeLater(() -> {
            disposeFrame("login");

            IngresoController ingresoController = new IngresoController(
                appContext.getUserService(),
                () -> showLoginFrame(),
                (parent, u, controller) -> new secureauth.ui.frames.EditUserFrame(parent, u, controller).setVisible(true)
            );

            SubServiceSelector subServiceSelector = (parent, serviceName, subServices) -> {
                secureauth.ui.dialogs.SubServiceDialog dialog = new secureauth.ui.dialogs.SubServiceDialog(parent, serviceName, subServices);
                dialog.setVisible(true);
                return dialog.getSelectedItem();
            };

            IngresoFrame mainFrame = new IngresoFrame(
                ingresoController,
                user,
                subServiceSelector,
                appContext
            );
            activeFrames.put("main", mainFrame);

            // Maximizar IngresoFrame
            mainFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            mainFrame.setVisible(true);
            LOGGER.info("IngresoFrame maximizado y mostrado desde WindowManager.");
        });
    }

    /**
     * Rutea de forma centralizada la navegación y conmutación de módulos en IngresoFrame.
     *
     * @param moduleName nombre identificador del panel/módulo
     */
    public void showModule(String moduleName) {
        JFrame mainFrame = activeFrames.get("main");
        if (mainFrame instanceof IngresoFrame ingreso) {
            SwingUtilities.invokeLater(() -> {
                try {
                    // Check if module is enabled according to business type
                    if (!SessionManager.getInstance().isModuleEnabled(moduleName)) {
                        LOGGER.warning("Intento de acceso a módulo deshabilitado para este rubro de negocio: " + moduleName);
                        return;
                    }
                    ingreso.showModule(moduleName);
                    LOGGER.info("Navegación al módulo '" + moduleName + "' coordinada exitosamente.");
                } catch (Exception e) {
                    LOGGER.severe("Error al conmutar al módulo '" + moduleName + "': " + e.getMessage());
                }
            });
        }
    }

    /**
     * Cierra y libera los recursos de memoria asociados a un frame específico.
     *
     * @param key identificador de la ventana ("login" o "main")
     */
    public void disposeFrame(String key) {
        JFrame frame = activeFrames.remove(key);
        if (frame != null) {
            frame.setVisible(false);
            frame.dispose();
            LOGGER.info("Ventana '" + key + "' destruida y memoria liberada.");
        }
    }

    public JFrame getFrame(String key) {
        return activeFrames.get(key);
    }
}
