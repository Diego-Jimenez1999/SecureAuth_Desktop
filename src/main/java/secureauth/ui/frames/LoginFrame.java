package secureauth.ui.frames;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.time.DateTimeException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import secureauth.controller.AuthController;
import secureauth.ui.utils.ComponentUtils;
import secureauth.ui.utils.FloatingPlaceholder;
import secureauth.ui.utils.RoundedLineBorder;
import secureauth.ui.utils.UiTheme;

/**
 * Ventana de autenticación y registro con layout responsivo.
 */
public class LoginFrame extends JFrame {

    private final AuthController controller;
    private final Consumer<secureauth.model.User> onLoginSuccess;

    private JTextField txtEmailLogin;
    private JPasswordField txtPasswordLogin;
    private JButton btnLogin;

    private JTextField txtEmail;
    private JPasswordField txtPassword;
    private JTextField txtNombre;
    private JTextField txtApellido;

    private JComboBox<String> cbDia;
    private JComboBox<String> cbMes;
    private JComboBox<String> cbAnio;


    private JRadioButton rbHombre;
    private JRadioButton rbMujer;
    private JRadioButton rbOtro;
    private JButton btnRegistrar;
    private JPanel imagePanel;
    private JPanel registerContainer;

    private JLabel errEmailReg;
    private JLabel errPasswordReg;
    private JLabel errNombreReg;
    private JLabel errApellidoReg;
    private JLabel errFechaReg;
    private JLabel errGeneroReg;
    private final List<FloatingPlaceholder> placeholders = new ArrayList<>();

    /**
     * Constructor principal de la ventana de autenticación.
     *
     * @param controller el controlador de autenticación
     * @param onLoginSuccess el callback que se ejecuta al iniciar sesión exitosamente
     */
    public LoginFrame(AuthController controller, Consumer<secureauth.model.User> onLoginSuccess) {
        this.controller = Objects.requireNonNull(controller, "AuthController es requerido");
        this.onLoginSuccess = Objects.requireNonNull(onLoginSuccess, "Callback de login es requerido");
        initComponents();
        setupFrame();
    }

    /**
     * Inicializa los componentes de la interfaz.
     */
    private void initComponents() {
        txtEmailLogin = new JTextField();
        txtPasswordLogin = new JPasswordField();
        btnLogin = new JButton("Iniciar");

        txtEmail = new JTextField();
        txtPassword = new JPasswordField();
        txtNombre = new JTextField();
        txtApellido = new JTextField();

        cbDia = new JComboBox<>(generarDias());
        cbMes = new JComboBox<>(new String[]{"Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"});
        cbAnio = new JComboBox<>(generarAnios());

        rbHombre = new JRadioButton("Hombre");
        rbMujer = new JRadioButton("Mujer");
        rbOtro = new JRadioButton("Otro");



        ButtonGroup grupoGenero = new ButtonGroup();
        grupoGenero.add(rbHombre);
        grupoGenero.add(rbMujer);
        grupoGenero.add(rbOtro);

        btnRegistrar = new JButton("Registrarse");

        errEmailReg = createErrorIndicator();
        errPasswordReg = createErrorIndicator();
        errNombreReg = createErrorIndicator();
        errApellidoReg = createErrorIndicator();
        errFechaReg = createErrorIndicator();
        errGeneroReg = createErrorIndicator();

        applyBaseStyles();

        setLayout(new BorderLayout());
        add(buildHeader(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
        installResponsiveBehavior();

        btnLogin.addActionListener(e -> login());
        btnRegistrar.addActionListener(e -> register());
    }

    /**
     * Aplica estilos base a los componentes.
     */
    private void applyBaseStyles() {
        getContentPane().setBackground(UiTheme.BG_LIGHT);

        styleTextComponent(txtEmailLogin, new Dimension(170, 43));
        styleTextComponent(txtPasswordLogin, new Dimension(170, 43));
        styleTextComponent(txtEmail, UiTheme.FIELD_SIZE_MEDIUM);
        styleTextComponent(txtPassword, UiTheme.FIELD_SIZE_MEDIUM);
        styleTextComponent(txtNombre, UiTheme.FIELD_SIZE_MEDIUM);
        styleTextComponent(txtApellido, UiTheme.FIELD_SIZE_MEDIUM);

        placeholders.add(new FloatingPlaceholder("Correo", txtEmailLogin));
        placeholders.add(new FloatingPlaceholder("Contraseña", txtPasswordLogin));
        placeholders.add(new FloatingPlaceholder("Correo electrónico", txtEmail));
        placeholders.add(new FloatingPlaceholder("Contraseña", txtPassword));
        placeholders.add(new FloatingPlaceholder("Nombre", txtNombre));
        placeholders.add(new FloatingPlaceholder("Apellido", txtApellido));

        styleComboBox(cbDia);
        styleComboBox(cbMes);
        styleComboBox(cbAnio);
        styleRadioButton(rbHombre);
        styleRadioButton(rbMujer);
        styleRadioButton(rbOtro);

        styleHeaderButton(btnLogin);
        styleRegisterButton(btnRegistrar);
    }

    /**
     * Construye el panel de encabezado con el logo a la izquierda y el formulario de login a la derecha.
     *
     * @return el panel de encabezado completamente construido
     */
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UiTheme.PANEL_WHITE);
        header.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel leftFlow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftFlow.setOpaque(false);
        JLabel logo = new JLabel();
        logo.setIcon(UiTheme.scaleImage("/logop_4.png", 260, 78));
        leftFlow.add(logo);

        JPanel rightFlow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightFlow.setOpaque(false);
        rightFlow.add(txtEmailLogin);
        rightFlow.add(txtPasswordLogin);
        rightFlow.add(btnLogin);

        header.add(leftFlow, BorderLayout.WEST);
        header.add(rightFlow, BorderLayout.EAST);
        return header;
    }

    /*
    * Construye el panel central con la imagen a la izquierda y el formulario de registro a la derecha. 
    * El panel de imagen se oculta automáticamente en ventanas pequeñas.
    * @return el panel central completamente construido
    */
    private JPanel buildCenter() {
        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(UiTheme.BG_LIGHT);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1;

        gbc.gridx = 0;
        gbc.weightx = 0.42;
        gbc.insets = new Insets(20, 25, 20, 10);
        imagePanel = buildImagePanel();
        center.add(imagePanel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.58;
        gbc.insets = new Insets(20, 10, 20, 25);
        registerContainer = buildRegisterContainer();
        center.add(registerContainer, gbc);

        return center;
    }

    private JPanel buildImagePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        JLabel image = new JLabel();
        image.setHorizontalAlignment(SwingConstants.CENTER);
        image.setIcon(UiTheme.scaleImage("/logo.png", 560, 430));
        image.setPreferredSize(new Dimension(560, 430));

        panel.add(image);
        return panel;
    }
    
    /**
     * Construye el contenedor del formulario de registro.
     *
     * @return el contenedor del formulario de registro completamente construido
     */
    private JPanel buildRegisterContainer() {
        JPanel container = new JPanel(new GridBagLayout());
        container.setOpaque(false);

        JPanel registerPanel = buildRegisterPanel();
        registerPanel.setPreferredSize(new Dimension(560, 540));
        registerPanel.setMinimumSize(new Dimension(510, 510));

        JScrollPane scroll = new JScrollPane(registerPanel);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setPreferredSize(new Dimension(580, 560));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 1;
        gbc.weighty = 1;
        container.add(scroll, gbc);

        return container;
    }

    private JPanel buildRegisterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UiTheme.PANEL_WHITE);
        panel.setBorder(new CompoundBorder(
                BorderFactory.createLineBorder(UiTheme.SUBTLE_BORDER, 1, true),
                new EmptyBorder(24, 24, 24, 24)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        JLabel lblTitulo = new JLabel("Registrarse", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(new Color(35, 35, 35));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 18, 0);
        panel.add(lblTitulo, gbc);

        gbc.gridwidth = 1;
        gbc.insets = new Insets(4, 5, 2, 5);
        gbc.gridy = 1;
        gbc.gridx = 0;
        panel.add(createLabelWithIndicator("Correo electrónico", errEmailReg), gbc);
        gbc.gridx = 1;
        panel.add(createLabelWithIndicator("Contraseña", errPasswordReg), gbc);

        gbc.gridy = 2;
        gbc.gridx = 0;
        panel.add(txtEmail, gbc);
        gbc.gridx = 1;
        panel.add(txtPassword, gbc);

        gbc.gridy = 3;
        gbc.gridx = 0;
        panel.add(createLabelWithIndicator("Nombre", errNombreReg), gbc);
        gbc.gridx = 1;
        panel.add(createLabelWithIndicator("Apellido", errApellidoReg), gbc);

        gbc.gridy = 4;
        gbc.gridx = 0;
        panel.add(txtNombre, gbc);
        gbc.gridx = 1;
        panel.add(txtApellido, gbc);

        gbc.gridy = 5;
        gbc.gridx = 0;
        panel.add(createLabelWithIndicator("Fecha de nacimiento", errFechaReg), gbc);

        gbc.gridx = 1;
        panel.add(buildFechaPanel(), gbc);

        gbc.gridy = 6;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        panel.add(createLabelWithIndicator("Género", errGeneroReg), gbc);

        JPanel generoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 0));
        generoPanel.setOpaque(false);
        generoPanel.add(rbHombre);
        generoPanel.add(rbMujer);
        generoPanel.add(rbOtro);

        gbc.gridy = 7;
        panel.add(generoPanel, gbc);

        gbc.gridy = 8;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(16, 5, 0, 5);
        panel.add(btnRegistrar, gbc);

        return panel;
    }

    private JPanel buildFechaPanel() {
        JPanel fechaPanel = new JPanel(new GridBagLayout());
        fechaPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 0, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        gbc.gridx = 0;
        fechaPanel.add(cbDia, gbc);
        gbc.gridx = 1;
        fechaPanel.add(cbMes, gbc);
        gbc.gridx = 2;
        gbc.insets = new Insets(0, 0, 0, 0);
        fechaPanel.add(cbAnio, gbc);

        return fechaPanel;
    }

    private void styleTextComponent(JTextField field, Dimension size) {
        ComponentUtils.styleTextField(field, size, UiTheme.BODY_FONT, UiTheme.FOREST_GREEN);
    }

    private void styleHeaderButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setBackground(UiTheme.DARK_SIDEBAR);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(110, 34));
    }

    private void styleRegisterButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setForeground(Color.WHITE);
        button.setBackground(UiTheme.FOREST_GREEN);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(230, 46));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(UiTheme.FOREST_GREEN_HOVER);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(UiTheme.FOREST_GREEN);
            }
        });
    }

    private void styleRadioButton(JRadioButton radioButton) {
        radioButton.setOpaque(false);
        radioButton.setFocusPainted(false);
        radioButton.setFont(UiTheme.BODY_FONT);
        radioButton.setForeground(new Color(45, 45, 45));
        radioButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void styleComboBox(JComboBox<String> comboBox) {
        comboBox.setFont(UiTheme.BODY_FONT);
        comboBox.setBackground(Color.WHITE);
        comboBox.setFocusable(false);
        comboBox.setPreferredSize(new Dimension(72, 38));
        comboBox.setBorder(new CompoundBorder(new RoundedLineBorder(new Color(196, 196, 196), 10, 1), new EmptyBorder(4, 8, 4, 8)));
    }

    private void setupFrame() {
        setTitle("SecureAuth - Registro de Usuarios");
        setMinimumSize(new Dimension(UiTheme.SIDEBAR_WIDTH * 5, 760));
        setSize(1180, 760);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    /**
     * En ventanas pequeñas prioriza visibilidad del formulario.
     */
    private void installResponsiveBehavior() {
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int width = getWidth();
                if (imagePanel == null || registerContainer == null) {
                    return;
                }
                if (width < 980) {
                    imagePanel.setVisible(false);
                } else {
                    imagePanel.setVisible(true);
                }
                revalidate();
                repaint();
            }
        });
    }
    
    /**
     * Crea un indicador de error.
     * @return el indicador de error
     *
     */
    private JLabel createErrorIndicator() {
        JLabel label = new JLabel("❓");
        label.setForeground(new Color(170, 0, 0));
        label.setVisible(false);
        label.setFont(UiTheme.BODY_FONT);
        return label;
    }
    
    /**
     * Crea un panel con una etiqueta y un indicador de error.
     * @param text el texto de la etiqueta
     * @param errorLabel el indicador de error
     * @return el panel creado
     */
    private JPanel createLabelWithIndicator(String text, JLabel errorLabel) {
        JPanel labelPanel = new JPanel(new BorderLayout());
        labelPanel.setOpaque(false);

        JLabel label = new JLabel(text);
        label.setFont(UiTheme.BODY_FONT);
        label.setForeground(new Color(50, 50, 50));

        labelPanel.add(label, BorderLayout.WEST);
        labelPanel.add(errorLabel, BorderLayout.EAST);
        return labelPanel;
    }

    /**
     * Limpia los indicadores de error del formulario de registro.
     */
    private void clearRegisterErrors() {
        errEmailReg.setVisible(false);
        errPasswordReg.setVisible(false);
        errNombreReg.setVisible(false);
        errApellidoReg.setVisible(false);
        errFechaReg.setVisible(false);
        errGeneroReg.setVisible(false);
    }

    /**
     * Valida que los campos obligatorios del registro estén completos. Muestra indicadores de error junto a cada campo faltante.
     * 
     * @return true si todos los campos obligatorios están completos, false si falta alguno
     */
    private boolean validateRegisterRequiredFields() {
        boolean valid = true;

        if (txtEmail.getText().trim().isEmpty()) {
            errEmailReg.setVisible(true);
            valid = false;
        }
        if (new String(txtPassword.getPassword()).trim().isEmpty()) {
            errPasswordReg.setVisible(true);
            valid = false;
        }
        if (txtNombre.getText().trim().isEmpty()) {
            errNombreReg.setVisible(true);
            valid = false;
        }
        if (txtApellido.getText().trim().isEmpty()) {
            errApellidoReg.setVisible(true);
            valid = false;
        }
        if (cbDia.getSelectedItem() == null || cbMes.getSelectedItem() == null || cbAnio.getSelectedItem() == null) {
            errFechaReg.setVisible(true);
            valid = false;
        }
        if (!rbHombre.isSelected() && !rbMujer.isSelected() && !rbOtro.isSelected()) {
            errGeneroReg.setVisible(true);
            valid = false;
        }

        return valid;
    }

    private String[] generarDias() {
        String[] dias = new String[31];
        for (int i = 0; i < 31; i++) {
            dias[i] = String.valueOf(i + 1);
        }
        return dias;
    }

    private String[] generarAnios() {
        String[] anios = new String[100];
        int startYear = java.time.Year.now().getValue();
        for (int i = 0; i < 100; i++) {
            anios[i] = String.valueOf(startYear - i);
        }
        return anios;
    }

    private int convertirMes(String mesTexto) {
        return switch (mesTexto) {
            case "Ene" -> 1;
            case "Feb" -> 2;
            case "Mar" -> 3;
            case "Abr" -> 4;
            case "May" -> 5;
            case "Jun" -> 6;
            case "Jul" -> 7;
            case "Ago" -> 8;
            case "Sep" -> 9;
            case "Oct" -> 10;
            case "Nov" -> 11;
            case "Dic" -> 12;
            default -> throw new IllegalArgumentException("Mes inválido");
        };
    }

    private java.time.LocalDate getFechaNacimiento() {
        try {
            int dia = Integer.parseInt((String) cbDia.getSelectedItem());
            String mesTexto = (String) cbMes.getSelectedItem();
            int anio = Integer.parseInt((String) cbAnio.getSelectedItem());
            int mes = convertirMes(mesTexto);
            return java.time.LocalDate.of(anio, mes, dia);
        } catch (DateTimeException | NumberFormatException | NullPointerException e) {
            throw new IllegalArgumentException("Fecha de nacimiento inválida");
        }
    }

    /**
     * Intenta iniciar sesión con las credenciales proporcionadas.
     */
    @SuppressWarnings("UseSpecificCatch")
    private void login() {
        try {
            String email = txtEmailLogin.getText();
            String password = new String(txtPasswordLogin.getPassword());

            if (email.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Ingrese correo y contraseña");
                return;
            }

            var user = controller.login(email, password);
            if (user != null) {
                JOptionPane.showMessageDialog(this, "Login exitoso");
                onLoginSuccess.accept(user);
                this.dispose();

            } else {
                JOptionPane.showMessageDialog(this, "Correo o contraseña incorrectos");
            }
        } catch (IllegalArgumentException | SQLException e) {
            String title = (e instanceof SQLException) ? "Error de BD" : "Validación";
            int type = (e instanceof SQLException) ? JOptionPane.ERROR_MESSAGE : JOptionPane.WARNING_MESSAGE;
            JOptionPane.showMessageDialog(this, e.getMessage(), title, type);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error inesperado: " + e.getMessage(), "Error Crítico", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Registra un nuevo usuario.
     */
    @SuppressWarnings("UseSpecificCatch")
    private void register() {
        try {
            clearRegisterErrors();
            if (!validateRegisterRequiredFields()) {
                JOptionPane.showMessageDialog(this, "Faltan datos obligatorios. Revisa los campos marcados con ❓");
                return;
            }

            secureauth.model.User user = new secureauth.model.User();
            user.setEmail(txtEmail.getText());
            user.setPassword(new String(txtPassword.getPassword()));
            user.setNombre(txtNombre.getText());
            user.setApellido(txtApellido.getText());
            user.setFechaNacimiento(getFechaNacimiento()); // Asegúrate que getFechaNacimiento() no devuelva null
            user.setRolId(3); // Por defecto, asigna el rol de Recepcionista (ID 3)

            if (rbHombre.isSelected()) {
                user.setGenero("M");
            } else if (rbMujer.isSelected()) {
                user.setGenero("F");
            } else if (rbOtro.isSelected()) {
                user.setGenero("O");
            } else {
                throw new IllegalArgumentException("Debe seleccionar un género.");
            }

            boolean success = controller.register(user);
            if (success) {
                JOptionPane.showMessageDialog(this, "¡Usuario registrado exitosamente!");
                onLoginSuccess.accept(user);
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Error al registrar");
            }
        } catch (IllegalArgumentException | SQLException e) {
            String title = (e instanceof SQLException) ? "Error de BD" : "Datos inválidos";
            int type = (e instanceof SQLException) ? JOptionPane.ERROR_MESSAGE : JOptionPane.WARNING_MESSAGE;
            JOptionPane.showMessageDialog(this, e.getMessage(), title, type);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error inesperado: " + e.getMessage(), "Error Crítico", JOptionPane.ERROR_MESSAGE);
        }
    }

    public List<FloatingPlaceholder> getPlaceholders() {
        return placeholders;
    }

}
