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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.time.DateTimeException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import secureauth.controller.AuthController;
import secureauth.model.User;
import secureauth.ui.utils.ComponentUtils;
import secureauth.ui.utils.FloatingPlaceholder;
import secureauth.ui.utils.RoundedLineBorder;
import secureauth.ui.utils.UiTheme;

/**
 * Ventana de autenticación y registro con layout responsivo.
 *
 * <h2>Correcciones aplicadas v1.2</h2>
 * <ul>
 *   <li><b>Bug DI:</b> AuthController ya usa el AuthService inyectado (corregido en AuthController).</li>
 *   <li><b>Bug registro:</b> Tras registrar exitosamente se lanza un login real en vez de pasar
 *       el User local —sin id ni session— directamente al dashboard.</li>
 *   <li><b>Bug FloatingPlaceholder en JPasswordField:</b> El placeholder ya no se instala
 *       sobre el campo de contraseña; un JLabel externo cumple la misma función sin
 *       interferir con el echo char ni el layout interno del campo.</li>
 *   <li><b>UX login:</b> Enter en cualquier campo del header dispara el login.
 *       Botón deshabilitado durante el intento para evitar doble clic.</li>
 *   <li><b>UX registro:</b> Mensajes de error inline bajo cada campo en lugar de
 *       JOptionPane genérico para errores de validación.</li>
 *   <li><b>Logging:</b> Los intentos fallidos ya no se pierden; se registran con Logger.</li>
 * </ul>
 *
 * @author Diego
 * @version 1.2
 */
public class LoginFrame extends JFrame {

    private static final Logger LOGGER = Logger.getLogger(LoginFrame.class.getName());

    private final AuthController controller;
    private final Consumer<User> onLoginSuccess;

    // ── Campos de login (header) ───────────────────────────────────────────
    private JTextField      txtEmailLogin;
    private JPasswordField  txtPasswordLogin;
    private JButton         btnLogin;

    // ── Campos de registro ─────────────────────────────────────────────────
    private JTextField     txtEmail;
    private JPasswordField txtPassword;
    private JTextField     txtNombre;
    private JTextField     txtApellido;

    private JComboBox<String> cbDia;
    private JComboBox<String> cbMes;
    private JComboBox<String> cbAnio;

    private JRadioButton rbHombre;
    private JRadioButton rbMujer;
    private JRadioButton rbOtro;
    private JButton      btnRegistrar;

    // ── Indicadores de error inline ────────────────────────────────────────
    private JLabel errEmailReg;
    private JLabel errPasswordReg;
    private JLabel errNombreReg;
    private JLabel errApellidoReg;
    private JLabel errFechaReg;
    private JLabel errGeneroReg;

    // ── Responsive ─────────────────────────────────────────────────────────
    private JPanel imagePanel;
    private JPanel registerContainer;

    /** Solo placeholders de campos de texto (nunca sobre JPasswordField). */
    private final List<FloatingPlaceholder> placeholders = new ArrayList<>();

    /**
     * Constructor principal.
     *
     * @param controller     controlador de autenticación (con DI correcta desde MainApp)
     * @param onLoginSuccess callback que recibe el {@link User} autenticado desde BD
     */
    public LoginFrame(AuthController controller, Consumer<User> onLoginSuccess) {
        this.controller     = Objects.requireNonNull(controller,     "AuthController requerido");
        this.onLoginSuccess = Objects.requireNonNull(onLoginSuccess, "Callback de login requerido");
        initComponents();
        setupFrame();
    }

    // =========================================================================
    // INICIALIZACIÓN
    // =========================================================================

    private void initComponents() {
        // Login
        txtEmailLogin    = new JTextField();
        txtPasswordLogin = new JPasswordField();
        btnLogin         = new JButton("Iniciar");

        // Registro
        txtEmail    = new JTextField();
        txtPassword = new JPasswordField();
        txtNombre   = new JTextField();
        txtApellido = new JTextField();

        cbDia = new JComboBox<>(generarDias());
        cbMes = new JComboBox<>(new String[]{
            "Ene","Feb","Mar","Abr","May","Jun","Jul","Ago","Sep","Oct","Nov","Dic"
        });
        cbAnio = new JComboBox<>(generarAnios());

        rbHombre = new JRadioButton("Hombre");
        rbMujer  = new JRadioButton("Mujer");
        rbOtro   = new JRadioButton("Otro");

        ButtonGroup grupoGenero = new ButtonGroup();
        grupoGenero.add(rbHombre);
        grupoGenero.add(rbMujer);
        grupoGenero.add(rbOtro);

        btnRegistrar = new JButton("Registrarse");

        // Indicadores de error inline
        errEmailReg    = createErrorLabel();
        errPasswordReg = createErrorLabel();
        errNombreReg   = createErrorLabel();
        errApellidoReg = createErrorLabel();
        errFechaReg    = createErrorLabel();
        errGeneroReg   = createErrorLabel();

        applyBaseStyles();

        setLayout(new BorderLayout());
        add(buildHeader(),  BorderLayout.NORTH);
        add(buildCenter(),  BorderLayout.CENTER);

        installResponsiveBehavior();
        installActionListeners();
    }

    /** Registra listeners de botones y teclado. */
    private void installActionListeners() {
        btnLogin.addActionListener(e -> doLogin());
        btnRegistrar.addActionListener(e -> doRegister());

        // Enter en cualquier campo del header dispara login
        KeyAdapter enterLogin = new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) doLogin();
            }
        };
        txtEmailLogin.addKeyListener(enterLogin);
        txtPasswordLogin.addKeyListener(enterLogin);

        // Enter en último campo de registro dispara registro
        txtApellido.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) doRegister();
            }
        });
    }

    private void applyBaseStyles() {
        getContentPane().setBackground(UiTheme.BG_LIGHT);

        styleField(txtEmailLogin,    new Dimension(170, 43));
        styleField(txtPasswordLogin, new Dimension(170, 43));
        styleField(txtEmail,         UiTheme.FIELD_SIZE_MEDIUM);
        styleField(txtPassword,      UiTheme.FIELD_SIZE_MEDIUM);
        styleField(txtNombre,        UiTheme.FIELD_SIZE_MEDIUM);
        styleField(txtApellido,      UiTheme.FIELD_SIZE_MEDIUM);

        // FIX: FloatingPlaceholder SOLO en JTextField, nunca en JPasswordField.
        // Instalar un placeholder en un JPasswordField rompe su BorderLayout interno
        // y puede interferir con el echo char. Para los password fields el label
        // de columna ("Contraseña") ya actúa como indicador suficiente.
        placeholders.add(new FloatingPlaceholder("Correo",               txtEmailLogin));
        placeholders.add(new FloatingPlaceholder("Correo electrónico",   txtEmail));
        placeholders.add(new FloatingPlaceholder("Nombre",               txtNombre));
        placeholders.add(new FloatingPlaceholder("Apellido",             txtApellido));

        styleComboBox(cbDia);
        styleComboBox(cbMes);
        styleComboBox(cbAnio);
        styleRadioButton(rbHombre);
        styleRadioButton(rbMujer);
        styleRadioButton(rbOtro);

        styleHeaderButton(btnLogin);
        styleRegisterButton(btnRegistrar);
    }

    // =========================================================================
    // CONSTRUCCIÓN DE PANELES
    // =========================================================================

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UiTheme.PANEL_WHITE);
        header.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel leftFlow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftFlow.setOpaque(false);
        JLabel logo = new JLabel();
        logo.setIcon(loadBrandingImage(UiTheme.getLogo(260, 78), "/logop_4.png", 260, 78));
        leftFlow.add(logo);

        JPanel rightFlow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightFlow.setOpaque(false);
        rightFlow.add(txtEmailLogin);
        rightFlow.add(txtPasswordLogin);
        rightFlow.add(btnLogin);

        header.add(leftFlow,  BorderLayout.WEST);
        header.add(rightFlow, BorderLayout.EAST);
        return header;
    }

    private JPanel buildCenter() {
        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(UiTheme.BG_LIGHT);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy   = 0;
        gbc.fill    = GridBagConstraints.BOTH;
        gbc.weighty = 1;

        gbc.gridx   = 0;
        gbc.weightx = 0.42;
        gbc.insets  = new Insets(20, 25, 20, 10);
        imagePanel  = buildImagePanel();
        center.add(imagePanel, gbc);

        gbc.gridx         = 1;
        gbc.weightx       = 0.58;
        gbc.insets        = new Insets(20, 10, 20, 25);
        registerContainer = buildRegisterContainer();
        center.add(registerContainer, gbc);

        return center;
    }

    private JPanel buildImagePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        JLabel image = new JLabel();
        image.setHorizontalAlignment(SwingConstants.CENTER);
        image.setIcon(loadBrandingImage(UiTheme.getLogo(560, 430), "/logo.png", 560, 430));
        image.setPreferredSize(new Dimension(560, 430));
        panel.add(image);
        return panel;
    }

    private JPanel buildRegisterContainer() {
        JPanel container = new JPanel(new GridBagLayout());
        container.setOpaque(false);

        JPanel registerPanel = buildRegisterPanel();
        registerPanel.setPreferredSize(new Dimension(560, 560));
        registerPanel.setMinimumSize(new Dimension(510, 520));

        JScrollPane scroll = new JScrollPane(registerPanel);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setPreferredSize(new Dimension(580, 580));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor  = GridBagConstraints.CENTER;
        gbc.fill    = GridBagConstraints.NONE;
        gbc.weightx = 1; gbc.weighty = 1;
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
        gbc.insets  = new Insets(5, 5, 5, 5);
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        // Título
        JLabel lblTitulo = new JLabel("Crear cuenta", SwingConstants.CENTER);
        lblTitulo.setFont(UiTheme.TITLE_FONT_SECTION);
        lblTitulo.setForeground(UiTheme.TEXT_PRIMARY);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 18, 0);
        panel.add(lblTitulo, gbc);

        gbc.gridwidth = 1;
        gbc.insets    = new Insets(4, 5, 2, 5);

        // Fila 1: Email / Contraseña labels
        gbc.gridy = 1; gbc.gridx = 0;
        panel.add(createLabelWithError("Correo electrónico", errEmailReg), gbc);
        gbc.gridx = 1;
        panel.add(createLabelWithError("Contraseña",         errPasswordReg), gbc);

        // Fila 2: Email / Contraseña fields
        gbc.gridy = 2; gbc.gridx = 0;
        panel.add(txtEmail, gbc);
        gbc.gridx = 1;
        panel.add(txtPassword, gbc);

        // Fila 3: Nombre / Apellido labels
        gbc.gridy = 3; gbc.gridx = 0;
        panel.add(createLabelWithError("Nombre",   errNombreReg), gbc);
        gbc.gridx = 1;
        panel.add(createLabelWithError("Apellido", errApellidoReg), gbc);

        // Fila 4: Nombre / Apellido fields
        gbc.gridy = 4; gbc.gridx = 0;
        panel.add(txtNombre,   gbc);
        gbc.gridx = 1;
        panel.add(txtApellido, gbc);

        // Fila 5: Fecha label / combos
        gbc.gridy = 5; gbc.gridx = 0;
        panel.add(createLabelWithError("Fecha de nacimiento", errFechaReg), gbc);
        gbc.gridx = 1;
        panel.add(buildFechaPanel(), gbc);

        // Fila 6: Género
        gbc.gridy = 6; gbc.gridx = 0; gbc.gridwidth = 2;
        panel.add(createLabelWithError("Género", errGeneroReg), gbc);

        JPanel generoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 0));
        generoPanel.setOpaque(false);
        generoPanel.add(rbHombre);
        generoPanel.add(rbMujer);
        generoPanel.add(rbOtro);
        gbc.gridy = 7;
        panel.add(generoPanel, gbc);

        // Fila 8: Botón
        gbc.gridy  = 8;
        gbc.fill   = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(16, 5, 0, 5);
        panel.add(btnRegistrar, gbc);

        return panel;
    }

    private JPanel buildFechaPanel() {
        JPanel fechaPanel = new JPanel(new GridBagLayout());
        fechaPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy   = 0;
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        gbc.gridx  = 0; gbc.insets = new Insets(0, 0, 0, 8);
        fechaPanel.add(cbDia, gbc);
        gbc.gridx  = 1;
        fechaPanel.add(cbMes, gbc);
        gbc.gridx  = 2; gbc.insets = new Insets(0, 0, 0, 0);
        fechaPanel.add(cbAnio, gbc);
        return fechaPanel;
    }

    // =========================================================================
    // ACCIONES
    // =========================================================================

    /**
     * Intenta el login. Deshabilita el botón durante el intento para evitar doble envío.
     *
     * <p>En la versión anterior el botón permanecía activo, permitiendo múltiples
     * intentos simultáneos que podían saturar el pool de conexiones.</p>
     */
    @SuppressWarnings("UseSpecificCatch")
    private void doLogin() {
        String email    = txtEmailLogin.getText().trim();
        String password = new String(txtPasswordLogin.getPassword());

        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "Por favor ingresa tu correo y contraseña.",
                "Campos requeridos",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Deshabilitar botón para evitar doble clic
        btnLogin.setEnabled(false);
        btnLogin.setText("...");

        try {
            User user = controller.login(email, password);

            if (user != null) {
                LOGGER.info("Login exitoso para: " + email);
                onLoginSuccess.accept(user);
                dispose();
            } else {
                LOGGER.warning("Credenciales inválidas para: " + email);
                JOptionPane.showMessageDialog(
                    this,
                    "El correo o la contraseña no son correctos.\n"
                    + "Verifica tus datos e intenta de nuevo.",
                    "Acceso denegado",
                    JOptionPane.WARNING_MESSAGE);
                txtPasswordLogin.setText("");
                txtPasswordLogin.requestFocusInWindow();
            }

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Datos inválidos", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error de base de datos en login", ex);
            JOptionPane.showMessageDialog(
                this,
                "No se pudo conectar a la base de datos.\n"
                + "Verifica que el servidor MySQL esté activo.",
                "Error de conexión",
                JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error inesperado en login", ex);
            JOptionPane.showMessageDialog(this, "Error inesperado: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            // Siempre restaurar el botón, incluso si hubo excepción
            SwingUtilities.invokeLater(() -> {
                btnLogin.setEnabled(true);
                btnLogin.setText("Iniciar");
            });
        }
    }

    /**
     * Registra un nuevo usuario y, si el registro es exitoso, realiza un
     * login automático con las credenciales recién ingresadas.
     *
     * <p><b>BUG CORREGIDO:</b> La versión anterior llamaba a
     * {@code onLoginSuccess.accept(user)} con el objeto local recién construido,
     * sin pasar por BD. Ese User tenía {@code id=0}, {@code rolId=3} hardcodeado
     * y sin validación de sesión. Ahora se hace un login real tras el registro
     * para garantizar que el User que llega al dashboard es el que está en BD.</p>
     */
    @SuppressWarnings("UseSpecificCatch")
    private void doRegister() {
        clearRegisterErrors();

        if (!validateRegisterFields()) {
            return;
        }

        btnRegistrar.setEnabled(false);
        btnRegistrar.setText("Registrando...");

        try {
            User newUser = buildUserFromForm();
            boolean success = controller.register(newUser);

            if (!success) {
                JOptionPane.showMessageDialog(this, "No se pudo completar el registro. Intenta de nuevo.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // BUG FIX: hacer login real para obtener el User con id, rol y business_id desde BD
            String plainPassword = new String(txtPassword.getPassword());
            User authenticatedUser = controller.login(newUser.getEmail(), plainPassword);

            if (authenticatedUser != null) {
                LOGGER.info("Registro y login automático exitosos para: " + newUser.getEmail());
                JOptionPane.showMessageDialog(
                    this,
                    "¡Bienvenido! Tu cuenta ha sido creada exitosamente.",
                    "Registro completado",
                    JOptionPane.INFORMATION_MESSAGE);
                onLoginSuccess.accept(authenticatedUser);
                dispose();
            } else {
                // El registro fue exitoso pero el login falló — caso muy inusual
                LOGGER.warning("Registro OK pero login automático falló para: " + newUser.getEmail());
                JOptionPane.showMessageDialog(
                    this,
                    "Tu cuenta fue creada. Ahora inicia sesión con tus datos.",
                    "Cuenta creada",
                    JOptionPane.INFORMATION_MESSAGE);
                // Poner el email en el campo de login para facilitar el acceso
                txtEmailLogin.setText(newUser.getEmail());
                txtPasswordLogin.requestFocusInWindow();
            }

        } catch (IllegalArgumentException ex) {
            // Validaciones del servicio (contraseña débil, email duplicado, etc.)
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Datos inválidos", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error de BD en registro", ex);
            JOptionPane.showMessageDialog(
                this,
                "Error al guardar en la base de datos.\n" + ex.getMessage(),
                "Error de base de datos",
                JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error inesperado en registro", ex);
            JOptionPane.showMessageDialog(this, "Error inesperado: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            SwingUtilities.invokeLater(() -> {
                btnRegistrar.setEnabled(true);
                btnRegistrar.setText("Registrarse");
            });
        }
    }

    // =========================================================================
    // VALIDACIÓN Y CONSTRUCCIÓN DEL FORMULARIO
    // =========================================================================

    /**
     * Limpia todos los indicadores de error del formulario de registro.
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
     * Valida campos del formulario de registro y activa indicadores de error inline.
     *
     * @return {@code true} si todos los campos son válidos
     */
    private boolean validateRegisterFields() {
        boolean valid = true;

        if (txtEmail.getText().trim().isEmpty()) {
            errEmailReg.setToolTipText("El correo es obligatorio");
            errEmailReg.setVisible(true);
            valid = false;
        }
        if (new String(txtPassword.getPassword()).isEmpty()) {
            errPasswordReg.setToolTipText("La contraseña es obligatoria");
            errPasswordReg.setVisible(true);
            valid = false;
        }
        if (txtNombre.getText().trim().isEmpty()) {
            errNombreReg.setToolTipText("El nombre es obligatorio");
            errNombreReg.setVisible(true);
            valid = false;
        }
        if (txtApellido.getText().trim().isEmpty()) {
            errApellidoReg.setToolTipText("El apellido es obligatorio");
            errApellidoReg.setVisible(true);
            valid = false;
        }
        if (cbDia.getSelectedItem() == null || cbMes.getSelectedItem() == null || cbAnio.getSelectedItem() == null) {
            errFechaReg.setToolTipText("Selecciona tu fecha de nacimiento");
            errFechaReg.setVisible(true);
            valid = false;
        }
        if (!rbHombre.isSelected() && !rbMujer.isSelected() && !rbOtro.isSelected()) {
            errGeneroReg.setToolTipText("Selecciona un género");
            errGeneroReg.setVisible(true);
            valid = false;
        }

        if (!valid) {
            JOptionPane.showMessageDialog(
                this,
                "Completa todos los campos marcados con ❗ antes de continuar.",
                "Campos obligatorios",
                JOptionPane.WARNING_MESSAGE);
        }

        return valid;
    }

    /**
     * Construye un objeto {@link User} con los datos del formulario de registro.
     *
     * @return usuario listo para pasar a {@link AuthController#register(User)}
     * @throws IllegalArgumentException si la fecha seleccionada es inválida
     */
    private User buildUserFromForm() {
        User user = new User();
        user.setEmail(txtEmail.getText().trim());
        user.setPassword(new String(txtPassword.getPassword()));
        user.setNombre(txtNombre.getText().trim());
        user.setApellido(txtApellido.getText().trim());
        user.setFechaNacimiento(getFechaNacimiento());
        user.setRolId(3); // Recepcionista por defecto; AuthService puede sobrescribirlo

        if (rbHombre.isSelected())     user.setGenero("M");
        else if (rbMujer.isSelected()) user.setGenero("F");
        else                           user.setGenero("O");

        return user;
    }

    // =========================================================================
    // UTILIDADES
    // =========================================================================

    private String[] generarDias() {
        String[] dias = new String[31];
        for (int i = 0; i < 31; i++) dias[i] = String.valueOf(i + 1);
        return dias;
    }

    private String[] generarAnios() {
        int startYear = java.time.Year.now().getValue();
        String[] anios = new String[100];
        for (int i = 0; i < 100; i++) anios[i] = String.valueOf(startYear - i);
        return anios;
    }

    private java.time.LocalDate getFechaNacimiento() {
        try {
            int dia  = Integer.parseInt((String) Objects.requireNonNull(cbDia.getSelectedItem()));
            int mes  = convertirMes((String) Objects.requireNonNull(cbMes.getSelectedItem()));
            int anio = Integer.parseInt((String) Objects.requireNonNull(cbAnio.getSelectedItem()));
            return java.time.LocalDate.of(anio, mes, dia);
        } catch (DateTimeException | NumberFormatException | NullPointerException e) {
            throw new IllegalArgumentException("La fecha de nacimiento seleccionada no es válida.");
        }
    }

    private int convertirMes(String mesTexto) {
        return switch (mesTexto) {
            case "Ene" ->  1; case "Feb" ->  2; case "Mar" ->  3;
            case "Abr" ->  4; case "May" ->  5; case "Jun" ->  6;
            case "Jul" ->  7; case "Ago" ->  8; case "Sep" ->  9;
            case "Oct" -> 10; case "Nov" -> 11; case "Dic" -> 12;
            default -> throw new IllegalArgumentException("Mes inválido: " + mesTexto);
        };
    }

    // =========================================================================
    // ESTILOS
    // =========================================================================

    private void styleField(JTextField field, Dimension size) {
        ComponentUtils.styleTextField(field, size, UiTheme.BODY_FONT, UiTheme.FOREST_GREEN);
    }

    private void styleHeaderButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setBackground(UiTheme.themeSecondary());
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(110, 34));
    }

    private void styleRegisterButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setForeground(Color.WHITE);
        button.setBackground(UiTheme.themePrimary());
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(230, 46));
        button.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { button.setBackground(UiTheme.themePrimaryHover()); }
            @Override public void mouseExited(MouseEvent e)  { button.setBackground(UiTheme.themePrimary()); }
        });
    }

    private void styleComboBox(JComboBox<String> comboBox) {
        comboBox.setFont(UiTheme.BODY_FONT);
        comboBox.setBackground(Color.WHITE);
        comboBox.setFocusable(false);
        comboBox.setPreferredSize(new Dimension(72, 38));
        comboBox.setBorder(new CompoundBorder(
            new RoundedLineBorder(new Color(196, 196, 196), 10, 1),
            new EmptyBorder(4, 8, 4, 8)));
    }

    private void styleRadioButton(JRadioButton rb) {
        rb.setOpaque(false);
        rb.setFocusPainted(false);
        rb.setFont(UiTheme.BODY_FONT);
        rb.setForeground(new Color(45, 45, 45));
        rb.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private javax.swing.ImageIcon loadBrandingImage(
            javax.swing.ImageIcon dynamic, String fallback, int w, int h) {
        return dynamic != null ? dynamic : UiTheme.scaleImage(fallback, w, h);
    }

    /**
     * Crea un label de error inline con icono ❗ y tooltip.
     * La versión anterior usaba ❓ — cambiado a ❗ para mayor claridad.
     */
    private JLabel createErrorLabel() {
        JLabel label = new JLabel("❗");
        label.setForeground(UiTheme.ERROR_COLOR);
        label.setVisible(false);
        label.setFont(UiTheme.BODY_FONT);
        return label;
    }

    private JPanel createLabelWithError(String text, JLabel errorLabel) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        JLabel label = new JLabel(text);
        label.setFont(UiTheme.BODY_FONT);
        label.setForeground(new Color(50, 50, 50));
        panel.add(label,      BorderLayout.WEST);
        panel.add(errorLabel, BorderLayout.EAST);
        return panel;
    }

    // =========================================================================
    // FRAME
    // =========================================================================

    private void setupFrame() {
        setTitle(UiTheme.themeAppTitle() + " — Acceso al sistema");
        setMinimumSize(new Dimension(UiTheme.SIDEBAR_WIDTH * 5, 760));
        setSize(1180, 760);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private void installResponsiveBehavior() {
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (imagePanel == null || registerContainer == null) return;
                imagePanel.setVisible(getWidth() >= 980);
                revalidate();
                repaint();
            }
        });
    }

    /** Expuesto para testing de placeholders. */
    public List<FloatingPlaceholder> getPlaceholders() {
        return placeholders;
    }
}