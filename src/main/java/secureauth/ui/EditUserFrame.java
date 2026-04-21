package secureauth.ui;

import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.time.LocalDate;
import java.time.Period;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import secureauth.controller.IngresoController;
import secureauth.model.User;
import secureauth.security.PasswordHasher;

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

    private final Color COLOR_ACCENT = new Color(198, 40, 40);
    private final Color COLOR_BORDER = new Color(198, 40, 40);
    private final Color COLOR_BG = new Color(244, 246, 249);

    // Campos editables
    private JTextField txtNombre, txtApellido, txtEmail;
    private JPasswordField txtPassword;
    private JComboBox<String> cmbGenero;

    // Indicadores de error
    private JLabel errNombre = new JLabel("❓");
    private JLabel errApellido = new JLabel("❓");
    private JLabel errEmail = new JLabel("❓");
    private JLabel errPassword = new JLabel("❓");

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
        setSize(600, 600);
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
        JPanel panel = new JPanel(new GridLayout(4, 1));
        panel.setBorder(new EmptyBorder(10, 20, 10, 20));

        panel.add(new JLabel("ID: " + user.getId()));
        panel.add(new JLabel("Edad: " + calcularEdad(user.getFechaNacimiento())));
        panel.add(new JLabel("Fecha Nacimiento: " + user.getFechaNacimiento()));
        panel.add(new JLabel("Nombre Completo: " + user.getNombre() + " " + user.getApellido()));

        return panel;
    }

    /**
     * Panel central con campos editables.
     */
    private JPanel createCenterPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        txtNombre = new JTextField();
        txtApellido = new JTextField();
        txtEmail = new JTextField();
        txtPassword = new JPasswordField();

        cmbGenero = new JComboBox<>(new String[]{"M", "F", "OTRO"});

        panel.add(createField("Nombre", txtNombre, errNombre));
        panel.add(createField("Apellido", txtApellido, errApellido));
        panel.add(createField("Correo", txtEmail, errEmail));
        panel.add(createField("Contraseña", txtPassword, errPassword));
        panel.add(createField("Género", cmbGenero, null));

        aplicarEventosFoco(txtNombre);
        aplicarEventosFoco(txtApellido);
        aplicarEventosFoco(txtEmail);
        aplicarEventosFoco(txtPassword);

        return panel;
    }

    /**
     * Crea un campo con su label y validación visual.
     */
    private JPanel createField(String label, JComponent field, JLabel error) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(5, 20, 5, 20));

        JLabel lbl = new JLabel(label);

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
        JPanel panel = new JPanel();

        JButton btnGuardar = new JButton("Guardar");
        JButton btnCancelar = new JButton("Cancelar");
        JButton btnEliminar = new JButton("Eliminar");

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
     */
    private void aplicarEventosFoco(JTextField campo) {
        campo.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                campo.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
            }

            public void focusLost(FocusEvent e) {
                campo.setBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1));
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
     */
    private int calcularEdad(LocalDate fecha) {
        if (fecha == null) return 0;
        return Period.between(fecha, LocalDate.now()).getYears();
    }
}
