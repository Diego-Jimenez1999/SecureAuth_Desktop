
package secureauth.ui.utils;

import java.awt.Cursor;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JPasswordField;

/**
 * <h2>PasswordFieldWithToggle</h2>
 * <p>Extensión personalizada de {@link JPasswordField} que integra un botón
 * visual para alternar la visibilidad de la contraseña.</p>
 * * <p><b>Uso de Recursos:</b> Requiere los archivos {@code eye_open.png} y
 * {@code eye_closed.png} en la carpeta de recursos.</p>
 * * @author Diego
 */
public class SecureVisibilityField extends JPasswordField {

    private JCheckBox toggle;
    private Icon showIcon;
    private Icon hideIcon;

    /**
     * Inicializa el campo de contraseña con el botón de visibilidad oculto.
     */
    public SecureVisibilityField() {
        initComponentsInternal();
    }

    /**
     * Configura el diseño interno, carga iconos y gestiona eventos de acción.
     * <p>Utiliza un {@code null layout} interno para posicionar el toggle al final del campo.</p>
     */
    private void initComponentsInternal() {
        setLayout(null);

        // Carga de recursos (Ruta absoluta desde el classpath)
        java.net.URL showURL = getClass().getResource("/eye_open.png");
        java.net.URL hideURL = getClass().getResource("/eye_closed.png");

        if (showURL != null) showIcon = new ImageIcon(showURL);
        if (hideURL != null) hideIcon = new ImageIcon(hideURL);

        toggle = new JCheckBox();
        
        // Ajuste dinámico del botón al cambiar el tamaño del campo
        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                // Posicionamiento manual del toggle (a la derecha)
                toggle.setBounds(getWidth() - 30, (getHeight() - 20) / 2, 20, 20);
            }
        });

        if (hideIcon != null) toggle.setIcon(hideIcon);
        if (showIcon != null) toggle.setSelectedIcon(showIcon);

        toggle.setBorder(null);
        toggle.setContentAreaFilled(false);
        toggle.setFocusPainted(false);
        toggle.setRolloverEnabled(true);
        toggle.setCursor(new Cursor(Cursor.HAND_CURSOR));

        add(toggle);
        setEchoChar('•');

        // Lógica de visibilidad
        toggle.addActionListener(e -> {
            if (toggle.isSelected()) {
                setEchoChar((char) 0); // Texto plano
            } else {
                setEchoChar('•'); // Oculto
            }
        });
    }

    /**
     * Obtiene la referencia directa a este componente como campo de contraseña.
     * @return {@code this}
     */
    public JPasswordField getPasswordField() {
        return this;
    }
}
