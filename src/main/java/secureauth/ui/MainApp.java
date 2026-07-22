package secureauth.ui;

import javax.swing.SwingUtilities;
import secureauth.ui.config.WindowManager;

/**
 * Punto de entrada oficial de SecureAuth Desktop.
 *
 * <p>Delega de forma inmediata la inicialización del sistema y control
 * de navegación al mediador centralizado {@link WindowManager}.</p>
 *
 * @author Jules
 * @version 1.0
 */
public final class MainApp {

    private MainApp() {}

    /**
     * Hilo principal de ejecución (Bootstrap de la aplicación Swing).
     *
     * @param args argumentos de línea de comandos
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> WindowManager.getInstance().showLogin());
    }
}
