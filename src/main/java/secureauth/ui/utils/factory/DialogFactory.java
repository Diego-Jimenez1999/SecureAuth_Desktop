package secureauth.ui.utils.factory;

import javax.swing.JOptionPane;
import java.awt.Component;

/** Fábrica de diálogos utilitarios reutilizables. */
public final class DialogFactory {

    private DialogFactory() { }

    public static void info(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void warn(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Validación", JOptionPane.WARNING_MESSAGE);
    }

    public static void error(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
