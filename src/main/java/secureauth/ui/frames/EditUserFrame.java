package secureauth.ui.frames;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.time.LocalDate;
import java.time.Period;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import secureauth.controller.IngresoController;
import secureauth.model.User;
import secureauth.security.PasswordHasher;
import secureauth.ui.utils.UiTheme;

/**
 * Diálogo encargado de permitir la edición de los datos
 * del usuario actualmente logueado.
 *
 * Se respeta el patrón MVC, delegando las operaciones
 * al controlador y manteniendo la UI desacoplada.
 */
public final class EditUserFrame extends JDialog {

    private final User user;
    private final IngresoController controller;

    private final Color COLOR_ACCENT = UiTheme.themePrimary();
    private final Color COLOR_DARK = new Color(30, 36, 48);
    private final Color COLOR_INPUT_BORDER = new Color(180, 180, 180);
    private final Color COLOR_BG = new Color(244, 246, 249);

    // Campos editables
    private JTextField txtNombre, txtApellido, txtEmail;
    private JPasswordField txtPassword;
    private JComboBox<String> cmbGenero;

    // Indicadores de error
    private final JLabel errNombre = new JLabel("❓");
    private final JLabel errApellido = new JLabel("❓");
    private final JLabel errEmail = new JLabel("❓");
    private final JLabel errPassword = new JLabel("❓");

    /**
     * Constructor principal del diálogo.
     *
     * @param parent ventana padre
     * @param user usuario logueado
     * @param controller controlador MVC
     */
    public EditUserFrame(JFrame parent, User user, IngresoController controller) {
        super(parent, "Editar Usuario", true);
        this.user = user;
        this.controller = controller;

        setupFrame();
        buildUI();
        cargarDatos(user);
    }

    /**
     * Configuración base de la ventana.
     */
    private void setupFrame() {
        setSize(940, 640);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
    }

    /**
     * Construye la interfaz completa.
     */
    private void buildUI() {
        getContentPane().setBackground(COLOR_BG);

        add(createTopPanel(), BorderLayout.NORTH);
        add(createCenterPanel(), BorderLayout.CENTER);
        add(createFooterPanel(), BorderLayout.SOUTH);
    }

    /**
     * Panel superior con datos no editables.
     */
    private JPanel createTopPanel() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 12));
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(16, 24, 8, 24));

        JLabel title = new JLabel("Actualizar Información del Usuario");
        title.setFont(new Font("SansSerif", Font.BOLD, 44));
        title.setForeground(new Color(122, 25, 25));
        wrapper.add(title, BorderLayout.NORTH);

        JPanel card = new JPanel(new BorderLayout(18, 0));
        card.setBackground(new Color(239, 241, 243));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 215, 220), 1),
                new EmptyBorder(16, 16, 16, 16)
        ));

        JLabel photoLabel = new JLabel(getProfileIcon());
        photoLabel.setPreferredSize(new Dimension(130, 130));
        card.add(photoLabel, BorderLayout.WEST);

        JPanel infoGrid = new JPanel(new GridLayout(2, 2, 24, 12));
        infoGrid.setOpaque(false);
        infoGrid.add(createInfoBlock("ID de Usuario", String.valueOf(user.getId())));
        infoGrid.add(createInfoBlock("Fecha de Nacimiento", String.valueOf(user.getFechaNacimiento())));
        infoGrid.add(createInfoBlock("Edad", String.valueOf(calcularEdad(user.getFechaNacimiento()))));
        infoGrid.add(createInfoBlock("Nombre", user.getNombre() + " " + user.getApellido()));

        card.add(infoGrid, BorderLayout.CENTER);
        wrapper.add(card, BorderLayout.CENTER);

        return wrapper;
    }

    /**
     * Panel central con campos editables.
     */
    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(8, 24, 10, 24));

        txtNombre = new JTextField();
        txtApellido = new JTextField();
        txtEmail = new JTextField();
        txtPassword = new JPasswordField();

        cmbGenero = new JComboBox<>(new String[]{"M", "F", "OTRO"});

        JPanel formCard = new JPanel(new GridBagLayout());
        formCard.setBackground(new Color(239, 241, 243));
        formCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 215, 220), 1),
                new EmptyBorder(14, 14, 14, 14)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weightx = 1.0;

        gbc.gridx = 0;
        gbc.gridy = 0;
        formCard.add(createField("Nombre", txtNombre, errNombre), gbc);

        gbc.gridx = 1;
        formCard.add(createField("Apellido", txtApellido, errApellido), gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        formCard.add(createField("Correo", txtEmail, errEmail), gbc);

        gbc.gridx = 1;
        formCard.add(createField("Contraseña", txtPassword, errPassword), gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        formCard.add(createField("Género", cmbGenero, null), gbc);

        GridBagConstraints rootGbc = new GridBagConstraints();
        rootGbc.gridx = 0;
        rootGbc.gridy = 0;
        rootGbc.weightx = 1.0;
        rootGbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(formCard, rootGbc);

        aplicarEventosFoco(txtNombre);
        aplicarEventosFoco(txtApellido);
        aplicarEventosFoco(txtEmail);
        aplicarEventosFoco(txtPassword);

        return panel;
    }

    /**
     * Crea un campo con su label y validación visual.
     * @param label texto del label
     * @param field componente de entrada (JTextField, JComboBox, etc.)
     * @param error JLabel para mostrar error (puede ser null si no se necesita)
     */
    private JPanel createField(String label, JComponent field, JLabel error) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(2, 2, 2, 2));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 20));
        lbl.setBorder(new EmptyBorder(0, 0, 6, 0));

        field.setPreferredSize(new Dimension(330, 42));
        if (field instanceof JComboBox<?>) {
            field.setFont(new Font("SansSerif", Font.PLAIN, 20));
            field.setBorder(BorderFactory.createLineBorder(COLOR_INPUT_BORDER, 1));
        } else if (field instanceof JTextField jTextField) {
            jTextField.setFont(new Font("SansSerif", Font.PLAIN, 18));
            jTextField.setBorder(BorderFactory.createLineBorder(COLOR_INPUT_BORDER, 1));
            jTextField.setPreferredSize(new Dimension(390, 50));
        }

        if (error != null) {
            error.setForeground(Color.RED);
            error.setVisible(false);
        }

        panel.add(lbl, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);

        if (error != null) {
            panel.add(error, BorderLayout.EAST);
        }

        return panel;
    }

    /**
     * Panel inferior con acciones.
     */
    private JPanel createFooterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 0, 10, 0));

        JButton btnGuardar = new JButton("Guardar Cambios");
        JButton btnCancelar = new JButton("Cancelar");
        JButton btnEliminar = new JButton("Eliminar");

        styleActionButton(btnGuardar, COLOR_ACCENT, Color.WHITE, 220, 46);
        styleActionButton(btnCancelar, COLOR_DARK, Color.WHITE, 140, 46);
        styleActionButton(btnEliminar, new Color(130, 130, 130), Color.WHITE, 140, 46);

        btnGuardar.addActionListener(e -> guardarCambios());
        btnCancelar.addActionListener(e -> dispose());
        btnEliminar.addActionListener(e -> eliminarUsuario());

        panel.add(btnGuardar);
        panel.add(btnCancelar);
        panel.add(btnEliminar);

        return panel;
    }

    /**
     * Carga los datos del usuario en los campos.
     * @param user el usuario cuyos datos se cargarán en el formulario
     */
    private void cargarDatos(User user) {
        txtNombre.setText(user.getNombre());
        txtApellido.setText(user.getApellido());
        txtEmail.setText(user.getEmail());
        txtPassword.setText("");
        txtPassword.setToolTipText("Dejar vacío para mantener la contraseña actual");
        cmbGenero.setSelectedItem(user.getGenero());
    }

    /**
     * Aplica eventos de foco visual.
     * Cuando un campo gana foco, se resalta su borde. Al perderlo, vuelve a la normalidad.
     * @param campo el campo al que se le aplicarán los eventos de foco
     */
    private void aplicarEventosFoco(JTextField campo) {
        campo.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                campo.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
            }

            @Override
            public void focusLost(FocusEvent e) {
                campo.setBorder(BorderFactory.createLineBorder(COLOR_INPUT_BORDER, 1));
            }
        });
    }

    /**
     * Valida todos los campos.
     */
    private boolean validarCampos() {
        boolean valido = true;

        valido &= validarCampo(txtNombre, errNombre);
        valido &= validarCampo(txtApellido, errApellido);
        valido &= validarCampo(txtEmail, errEmail);

        String nuevaPassword = new String(txtPassword.getPassword()).trim();
        if (!nuevaPassword.isEmpty() && nuevaPassword.length() < 8) {
            txtPassword.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
            errPassword.setToolTipText("Si escribe contraseña, debe tener al menos 8 caracteres");
            errPassword.setVisible(true);
            valido = false;
        } else {
            errPassword.setVisible(false);
        }

        return valido;
    }

    /**
     * Valida un campo individual.
     */
    private boolean validarCampo(JTextField campo, JLabel error) {
        if (campo.getText().trim().isEmpty()) {
            campo.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
            error.setVisible(true);
            return false;
        }
        error.setVisible(false);
        return true;
    }

    /**
     * Maneja el guardado de datos.
     */
    private void guardarCambios() {
        if (!validarCampos()) {
            JOptionPane.showMessageDialog(this, "Hay campos vacíos");
            return;
        }

        actualizarUsuario();

        JOptionPane.showMessageDialog(this, "Usuario actualizado");
        dispose();
    }

    /**
     * Actualiza el modelo y delega al controlador.
     */
    private void actualizarUsuario() {
        user.setNombre(txtNombre.getText());
        user.setApellido(txtApellido.getText());
        user.setEmail(txtEmail.getText());
        String nuevaPassword = new String(txtPassword.getPassword()).trim();
        if (!nuevaPassword.isEmpty()) {
            user.setPassword(PasswordHasher.hash(nuevaPassword));
        }
        user.setGenero((String) cmbGenero.getSelectedItem());

        if (controller != null) {
            controller.actualizarUsuario(user);
        }
    }

    /**
     * Maneja la eliminación del usuario.
     */
    private void eliminarUsuario() {
        int confirm = JOptionPane.showConfirmDialog(this, "¿Eliminar usuario?");
        if (confirm == JOptionPane.YES_OPTION) {
            controller.eliminarUsuario(user.getId());
            dispose();
        }
    }

    /**
     * Calcula la edad del usuario.
     * @param fecha fecha de nacimiento
     * @return edad del usuario
     */
    private int calcularEdad(LocalDate fecha) {
        if (fecha == null) return 0;
        return Period.between(fecha, LocalDate.now()).getYears();
    }
    
    private ImageIcon getProfileIcon() {
        String resourcePath = "M".equalsIgnoreCase(user.getGenero()) ? "/sujetoM.png" : "/sujetoF.png";
        java.net.URL imageUrl = getClass().getResource(resourcePath);

        if (imageUrl == null) {
            return new ImageIcon();
        }

        Image image = new ImageIcon(imageUrl).getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH);
        return new ImageIcon(image);
    }

    /**
     * Crea un bloque de información.
     * @param title título del bloque
     * @param value valor a mostrar
    */
    private JPanel createInfoBlock(String title, String value) {
        JPanel block = new JPanel();
        block.setOpaque(false);
        block.setLayout(new BoxLayout(block, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.PLAIN, 18));
        titleLabel.setForeground(new Color(90, 90, 90));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        valueLabel.setForeground(new Color(30, 30, 30));

        block.add(titleLabel);
        block.add(Box.createVerticalStrut(4));
        block.add(valueLabel);
        return block;
    }

    /**
     * Aplica estilos a los botones de acción.
     * @param button el botón a estilizar
     * @param background color de fondo
     * @param foreground color de texto
     * @param width ancho preferido
     * @param height alto preferido
     */
    private void styleActionButton(JButton button, Color background, Color foreground, int width, int height) {
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(new Font("SansSerif", Font.BOLD, 21));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(width, height));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(background.darker(), 1),
                new EmptyBorder(4, 10, 4, 10)
        ));
    }
}
