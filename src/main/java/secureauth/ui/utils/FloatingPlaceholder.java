package secureauth.ui.utils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.text.JTextComponent;

/**
 * <h2>TextPrompt</h2>
 * <p>Proporciona un placeholder (etiqueta de sugerencia) profesional para componentes de texto.</p>
 * * <p><b>Características:</b></p>
 * <ul>
 * <li>No interfiere con el Layout original del componente.</li>
 * <li>Se oculta automáticamente al escribir o al ganar el foco.</li>
 * <li>Usa DocumentListener para reaccionar a cambios programáticos de texto.</li>
 * </ul>
 * * <pre>{@code
 * JTextField txtNombre = new JTextField();
 * TextPrompt placeholder = new TextPrompt("Ingrese su nombre...", txtNombre);
 * }</pre>
 * * @author Diego
 * @version 1.1
 */
public class FloatingPlaceholder extends JLabel {

    private final JTextComponent component;

    /**
     * Construye un nuevo TextPrompt asociado a un componente de texto.
     * @param text El texto que se mostrará como sugerencia.
     * @param component El {@link JTextComponent} (JTextField, JPasswordField) donde se aplicará.
     */
    public FloatingPlaceholder(String text, JTextComponent component) {
        super(text);
        this.component = component;

        setForeground(Color.GRAY);
        setFont(component.getFont());

        // Configuración del layout del componente padre
        component.setLayout(new BorderLayout());
        component.add(this);

        // Margen para que el texto no pegue con el borde del input
        setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));

        // Listeners de Foco
        component.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                check();
            }

            @Override
            public void focusLost(FocusEvent e) {
                check();
            }
        });

        // Listener de Documento (Detecta cuando se escribe o borra)
        component.getDocument().addDocumentListener((SimpleDocumentListener) e -> check());

        check();
    }

    /**
     * Evalúa si el placeholder debe ser visible basándose en el contenido
     * y el estado del foco del componente.
     */
    private void check() {
        setVisible(component.getText().isEmpty() && !component.hasFocus());
    }

    /**
     * Interface funcional interna para simplificar el uso de DocumentListener.
     */
    @FunctionalInterface
    interface SimpleDocumentListener extends javax.swing.event.DocumentListener {
        void update(javax.swing.event.DocumentEvent e);

        @Override
        default void insertUpdate(javax.swing.event.DocumentEvent e) { update(e); }
        @Override
        default void removeUpdate(javax.swing.event.DocumentEvent e) { update(e); }
        @Override
        default void changedUpdate(javax.swing.event.DocumentEvent e) { update(e); }
    }
}