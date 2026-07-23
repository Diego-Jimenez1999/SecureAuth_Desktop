package secureauth.shared.error;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.util.logging.Level;
import java.util.logging.Logger;
import secureauth.dao.enterprise.ActividadRecienteDAO;

/**
 * Manejador centralizado de errores y excepciones para el sistema.
 *
 * Centraliza la presentación visual de alertas al usuario y el registro
 * en logs de la aplicación.
 */
public final class ErrorHandler {

    private static final Logger LOGGER = Logger.getLogger(ErrorHandler.class.getName());
    private static final ActividadRecienteDAO ACTIVIDAD_DAO = new ActividadRecienteDAO();

    private ErrorHandler() {}

    /**
     * Maneja una excepción, registrándola y mostrando un diálogo visual.
     *
     * @param throwable excepción o error
     * @param context contexto o módulo donde ocurrió el error
     */
    public static void handleException(Throwable throwable, String context) {
        LOGGER.log(Level.SEVERE, "Error en contexto: " + context, throwable);

        // Intenta registrar el error en la actividad reciente
        try {
            String desc = "ERROR en " + context + ": " + throwable.getMessage();
            if (desc.length() > 300) {
                desc = desc.substring(0, 297) + "...";
            }
            ACTIVIDAD_DAO.insert(desc, "SISTEMA", "SYSTEM");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "No fue posible registrar el error en actividad_reciente.", e);
        }

        // Muestra alerta visual limpia al usuario en el EDT
        SwingUtilities.invokeLater(() -> {
            String displayMessage = "Ocurrió un error inesperado en: " + context + "\n\nDetalle: " + throwable.getMessage();
            JOptionPane.showMessageDialog(null, displayMessage, "Error del Sistema", JOptionPane.ERROR_MESSAGE);
        });
    }

    /**
     * Muestra una advertencia al usuario.
     *
     * @param message mensaje de advertencia
     * @param title título del diálogo
     */
    public static void showWarning(String message, String title) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(null, message, title, JOptionPane.WARNING_MESSAGE);
        });
    }

    /**
     * Muestra un mensaje informativo al usuario.
     *
     * @param message mensaje de información
     * @param title título del diálogo
     */
    public static void showInfo(String message, String title) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE);
        });
    }
}
