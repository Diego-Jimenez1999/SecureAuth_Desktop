package secureauth.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import secureauth.controller.AuthController;
import secureauth.model.User;
import secureauth.ui.utils.ComponentUtils;
import secureauth.ui.utils.FloatingPlaceholder;
import secureauth.ui.utils.UiTheme;

/**
 * =========================================================
 * CLASE: RegistroTrabajadores
 * =========================================================
 *
 * Ventana modal utilizada para registrar o editar trabajadores
 * dentro del sistema SecureAuth.
 *
 * Esta clase:
 *
 * - Construye toda la interfaz gráfica del formulario.
 * - Organiza los componentes usando GridBagLayout.
 * - Valida campos obligatorios.
 * - Gestiona el registro de usuarios.
 * - Maneja eventos visuales y estilos UI.
 *
 * =========================================================
 * COMPONENTES PRINCIPALES
 * =========================================================
 *
 * Campos:
 * - Correo electrónico
 * - Contraseña
 * - Nombre
 * - Apellido
 * - Fecha de nacimiento
 * - Rol
 * - Género
 *
 * =========================================================
 * TECNOLOGÍAS Y CONCEPTOS UTILIZADOS
 * =========================================================
 *
 * - Swing
 * - GridBagLayout
 * - JScrollPane
 * - JDialog Modal
 * - MVC
 * - Validaciones
 * - Placeholders flotantes
 *
 * =========================================================
 *
 * @author Diego
 * @version 2.0
 */
public class RegistroTrabajadores extends JDialog {

/* =========================================================
* CONSTANTES VISUALES
* ========================================================= */

     /**
     * Fuente base utilizada en todo el formulario.
     */
      private static final Font BASE_FONT =
                new Font("Segoe UI", Font.PLAIN, 14);

    /**
     * Tamaño estándar para TODOS los campos del formulario.
     *
     * Esto evita:
     * - tamaños diferentes
     * - deformaciones
     * - campos desalineados
     */
    private static final Dimension FIELD_SIZE =
                new Dimension(320, 44);

    /**
     * Tamaño estándar del botón principal.
     */
    private static final Dimension BUTTON_SIZE =
                new Dimension(250, 48);

    /* =========================================================
     * CONTROLADOR
     * ========================================================= */

    /**
     * Controlador encargado de la lógica de autenticación.
     */
    private final AuthController controller;

    /**
     * Callback opcional cuando el registro es exitoso.
     */
    private final Consumer<User> onLoginSuccess = null;

    /* =========================================================
     * CAMPOS DE TEXTO
     * ========================================================= */

    private JTextField txtEmail;
    private JPasswordField txtPassword;
    private JTextField txtNombre;
    private JTextField txtApellido;

    /* =========================================================
     * COMPONENTES FECHA
     * ========================================================= */

    private JComboBox<String> cbDia;
    private JComboBox<String> cbMes;
    private JComboBox<String> cbAnio;

    /* =========================================================
     * COMPONENTES GÉNERO
     * ========================================================= */

    private JRadioButton rbHombre;
    private JRadioButton rbMujer;
    private JRadioButton rbOtro;

    /* =========================================================
     * COMPONENTES ROL
     * ========================================================= */

    private JComboBox<String> rolBox;

    /* =========================================================
     * BOTÓN PRINCIPAL
     * ========================================================= */

    private JButton btnRegistrar;

    /* =========================================================
     * INDICADORES DE ERROR
     * ========================================================= */

    private JLabel errEmailReg;
    private JLabel errPasswordReg;
    private JLabel errNombreReg;
    private JLabel errApellidoReg;
    private JLabel errFechaReg;
    private JLabel errGeneroReg;
    private JLabel errRolReg;

    /**
     * Lista de placeholders flotantes.
     */
    private final List<FloatingPlaceholder> placeholders =
                new ArrayList<>();

    /* =========================================================
     * CONSTRUCTOR
     * ========================================================= */

    /**
     * Constructor principal del diálogo.
     *
     * @param parent     ventana padre
     * @param controller controlador de autenticación
     */
    public RegistroTrabajadores( java.awt.Frame parent,AuthController controller) {

        super(parent, "Registrar Trabajador", true);
        this.controller = controller;

        setupFrame();
        initComponents();
    }

    /* =========================================================
     * CONFIGURACIÓN DEL FRAME
     * ========================================================= */

    /**
     * Configura propiedades principales del diálogo.
     */
    private void setupFrame() {

        setSize(1100, 760);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

    }

    /* =========================================================
     * INICIALIZACIÓN
     * ========================================================= */

    /**
     * Inicializa todos los componentes visuales.
     */
    private void initComponents() {

        initializeFields();
        initializeDateComponents();
        initializeGenderComponents();
        initializeRoleComponent();
        initializeButton();
        initializeErrorIndicators();

        applyBaseStyles();

        add(buildMainContainer(), BorderLayout.CENTER);

        btnRegistrar.addActionListener(e -> register());

    }

    /* =========================================================
     * CREACIÓN DE COMPONENTES
     * ========================================================= */

    /**
     * Inicializa campos de texto.
     */
    private void initializeFields() {

        txtEmail = new JTextField();
        txtPassword = new JPasswordField();
        txtNombre = new JTextField();
        txtApellido = new JTextField();

    }

    /**
     * Inicializa componentes de fecha.
     */
    private void initializeDateComponents() {

        cbDia = new JComboBox<>(generateDays());

        cbMes = new JComboBox<>(new String[]{
                "Ene", "Feb", "Mar",
                "Abr", "May", "Jun",
                "Jul", "Ago", "Sep",
                "Oct", "Nov", "Dic"
        });

        cbAnio = new JComboBox<>(generateYears());

    }

    /**
     * Inicializa radio buttons de género.
     */
    private void initializeGenderComponents() {

        rbHombre = new JRadioButton("Hombre");
        rbMujer = new JRadioButton("Mujer");
        rbOtro = new JRadioButton("Otro");

        ButtonGroup group = new ButtonGroup();

        group.add(rbHombre);
        group.add(rbMujer);
        group.add(rbOtro);

    }

    /**
     * Inicializa combo box de roles.
     */
    private void initializeRoleComponent() {

        rolBox = new JComboBox<>(new String[]{
                "Administrador",
                "Recepcionista",
                "Médico"
        });

    }

    /**
     * Inicializa botón principal.
     */
    private void initializeButton() {

        btnRegistrar = new JButton("Registrar Usuario");

    }

    /**
     * Inicializa indicadores visuales de error.
     */
    private void initializeErrorIndicators() {

        errEmailReg = createErrorIndicator();
        errPasswordReg = createErrorIndicator();
        errNombreReg = createErrorIndicator();
        errApellidoReg = createErrorIndicator();
        errFechaReg = createErrorIndicator();
        errGeneroReg = createErrorIndicator();
        errRolReg = createErrorIndicator();

    }

    /* =========================================================
     * ESTILOS
     * ========================================================= */

    /**
     * Aplica estilos visuales base.
     */
    private void applyBaseStyles() {

        getContentPane().setBackground(UiTheme.BG_LIGHT);

        styleTextFields();
        styleComboBoxes();
        styleRadioButtons();
        styleRegisterButton(btnRegistrar);

        initializePlaceholders();

    }

    /**
     * Aplica estilos a todos los campos de texto.
     */
        private void styleTextFields() {

        ComponentUtils.styleTextField(
                txtEmail,
                FIELD_SIZE,
                BASE_FONT,
                UiTheme.FOREST_GREEN
        );

        ComponentUtils.styleTextField(
                txtPassword,
                FIELD_SIZE,
                BASE_FONT,
                UiTheme.FOREST_GREEN
        );

        ComponentUtils.styleTextField(
                txtNombre,
                FIELD_SIZE,
                BASE_FONT,
                UiTheme.FOREST_GREEN
        );

        ComponentUtils.styleTextField(
                txtApellido,
                FIELD_SIZE,
                BASE_FONT,
                UiTheme.FOREST_GREEN
        );

        }

        /**
             * Aplica estilos a los ComboBox.
        */
        private void styleComboBoxes() {

        ComponentUtils.styleComboBox(
                cbDia,
                FIELD_SIZE,
                BASE_FONT
        );

        ComponentUtils.styleComboBox(
                cbMes,
                FIELD_SIZE,
                BASE_FONT
        );

        ComponentUtils.styleComboBox(
                cbAnio,
                FIELD_SIZE,
                BASE_FONT
        );

        ComponentUtils.styleComboBox(
                rolBox,
                FIELD_SIZE,
                BASE_FONT
        );

    }

    /**
     * Aplica estilos a los radio buttons.
     */
    private void styleRadioButtons() {

        ComponentUtils.styleRadioButton(rbHombre);
        ComponentUtils.styleRadioButton(rbMujer);
        ComponentUtils.styleRadioButton(rbOtro);

    }

    /**
     * Inicializa placeholders flotantes.
     */
    private void initializePlaceholders() {

        placeholders.add(
                new FloatingPlaceholder(
                        "Correo electrónico",
                        txtEmail
                )
        );

        placeholders.add(
                new FloatingPlaceholder(
                        "Contraseña",
                        txtPassword
                )
        );

        placeholders.add(
                new FloatingPlaceholder(
                        "Nombre",
                        txtNombre
                )
        );

        placeholders.add(
                new FloatingPlaceholder(
                        "Apellido",
                        txtApellido
                )
        );

    }

    /* =========================================================
     * CONTENEDOR PRINCIPAL
     * ========================================================= */

    /**
     * Construye el contenedor principal con scroll.
     *
     * @return panel contenedor
     */
    private JPanel buildMainContainer() {

        JPanel container =
                new JPanel(new BorderLayout());

        container.setOpaque(false);

        JPanel registerPanel = buildRegisterPanel();

        JScrollPane scroll =
                new JScrollPane(registerPanel);

        scroll.setBorder(
                BorderFactory.createEmptyBorder()
        );

        scroll.setOpaque(false);

        scroll.getViewport().setOpaque(false);

        container.add(scroll, BorderLayout.CENTER);

        return container;

    }

    /* =========================================================
     * PANEL DEL FORMULARIO
     * ========================================================= */

    /**
     * Construye el formulario principal.
     *
     * @return panel del formulario
     */
    private JPanel buildRegisterPanel() {

        JPanel panel =
                new JPanel(new GridBagLayout());

        panel.setBackground(UiTheme.PANEL_WHITE);

        panel.setBorder(
                new EmptyBorder(40, 60, 40, 60)
        );

        GridBagConstraints gbc =
                new GridBagConstraints();

        gbc.insets = new Insets(7, 14, 7, 14);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        addTitle(panel, gbc);

        addEmailAndPassword(panel, gbc);

        addNameAndLastName(panel, gbc);

        addDateAndRole(panel, gbc);

        addGenderSection(panel, gbc);

        addRegisterButton(panel, gbc);

        return panel;

    }

    /* =========================================================
     * SECCIONES UI
     * ========================================================= */

    /**
     * Agrega el título principal.
     *
     * @param panel panel destino
     * @param gbc   constraints
     */
    private void addTitle(
            JPanel panel,
            GridBagConstraints gbc
    ) {

        JLabel title =
                new JLabel(
                        "Registro de Trabajadores",
                        SwingConstants.CENTER
                );

        title.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        30
                )
        );

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(8, 10, 28, 10);

        panel.add(title, gbc);

    }

        /**
              * Agrega sección email/password.
        */
        private void addEmailAndPassword(JPanel panel,GridBagConstraints gbc) {

        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        panel.add(createFormField("Correo electrónico", txtEmail, errEmailReg), gbc);

        gbc.gridx = 1;
        panel.add(createFormField("Contraseña", txtPassword, errPasswordReg), gbc);

    }

    /**
     * Agrega sección nombre/apellido.
     */
    private void addNameAndLastName(
            JPanel panel,
            GridBagConstraints gbc
    ) {

        gbc.gridy = 2;

        gbc.gridx = 0;
        panel.add(createFormField("Nombre", txtNombre, errNombreReg), gbc);

        gbc.gridx = 1;
        panel.add(createFormField("Apellido", txtApellido, errApellidoReg), gbc);

    }

    /**
     * Agrega sección fecha/rol.
     */
    private void addDateAndRole(
            JPanel panel,
            GridBagConstraints gbc
    ) {

        gbc.gridy = 3;

        gbc.gridx = 0;
        panel.add(createFormField("Fecha de nacimiento", buildDatePanel(), errFechaReg), gbc);

        gbc.gridx = 1;
        panel.add(createFormField("Rol", rolBox, errRolReg), gbc);

    }

    /**
     * Agrega sección género.
     */
    private void addGenderSection(
            JPanel panel,
            GridBagConstraints gbc
    ) {

        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.gridwidth = 2;

        JPanel genderPanel =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.CENTER,
                                50,
                                5
                        )
                );

        genderPanel.setOpaque(false);

        genderPanel.add(rbHombre);
        genderPanel.add(rbMujer);
        genderPanel.add(rbOtro);

        panel.add(createFormField("Género", genderPanel, errGeneroReg), gbc);

    }

    /**
     * Agrega botón principal.
     */
    private void addRegisterButton(
            JPanel panel,
            GridBagConstraints gbc
    ) {

        gbc.gridy = 5;

        gbc.insets =
                new Insets(26, 10, 14, 10);

        gbc.fill = GridBagConstraints.NONE;

        panel.add(btnRegistrar, gbc);

    }

    /* =========================================================
     * PANEL FECHA
     * ========================================================= */

    /**
     * Construye panel de fecha.
     *
     * @return panel fecha
     */
    private JPanel buildDatePanel() {

        JPanel panel =
                new JPanel(new GridBagLayout());

        panel.setOpaque(false);

        GridBagConstraints gbc =
                new GridBagConstraints();

        gbc.insets = new Insets(0, 0, 0, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        /*
         * Todos los ComboBox tendrán exactamente
         * el mismo tamaño visual.
         */

        cbDia.setPreferredSize(new Dimension(95, 44));
        cbMes.setPreferredSize(new Dimension(95, 44));
        cbAnio.setPreferredSize(new Dimension(110, 44));

        gbc.gridx = 0;
        panel.add(cbDia, gbc);

        gbc.gridx = 1;
        panel.add(cbMes, gbc);

        gbc.gridx = 2;
        panel.add(cbAnio, gbc);

        return panel;

    }

    /* =========================================================
     * LABELS
     * ========================================================= */

    /**
     * Crea un label con indicador de error.
     *
     * @param text texto del label
     * @param errorLabel indicador error
     * @return panel generado
     */
    private JPanel createLabelWithIndicator(
            String text,
            JLabel errorLabel
    ) {

        JPanel panel =
                new JPanel(new BorderLayout());

        panel.setOpaque(false);

        JLabel label = new JLabel(text);

        label.setFont(BASE_FONT);

        label.setForeground(new Color(40, 40, 40));

        panel.add(label, BorderLayout.WEST);
        panel.add(errorLabel, BorderLayout.EAST);

        return panel;

    }

    private JPanel createFormField(String label, JComponent input, JLabel errorLabel) {

        JPanel panel =
                new JPanel();

        panel.setOpaque(false);
        panel.setLayout(new javax.swing.BoxLayout(panel, javax.swing.BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));

        JPanel labelPanel = createLabelWithIndicator(label, errorLabel);
        labelPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        input.setAlignmentX(Component.LEFT_ALIGNMENT);
        input.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        panel.add(labelPanel);
        panel.add(javax.swing.Box.createVerticalStrut(4));
        panel.add(input);

        return panel;

    }

    /* =========================================================
     * ERRORES
     * ========================================================= */

    /**
     * Crea un indicador visual de error.
     *
     * @return JLabel error
     */
    private JLabel createErrorIndicator() {

        JLabel label = new JLabel("❓");

        label.setForeground(new Color(170, 0, 0));

        label.setVisible(false);

        label.setFont(BASE_FONT);

        return label;

    }

    /**
     * Limpia todos los errores.
     */
    private void clearRegisterErrors() {

        errEmailReg.setVisible(false);
        errPasswordReg.setVisible(false);
        errNombreReg.setVisible(false);
        errApellidoReg.setVisible(false);
        errFechaReg.setVisible(false);
        errGeneroReg.setVisible(false);
        errRolReg.setVisible(false);

    }

    /* =========================================================
     * VALIDACIONES
     * ========================================================= */

    /**
     * Valida campos obligatorios.
     *
     * @return true si es válido
     */
    private boolean validateRegisterRequiredFields() {

        boolean valid = true;

        if (txtEmail.getText().trim().isEmpty()) {

            errEmailReg.setVisible(true);
            valid = false;

        }

        if (new String(txtPassword.getPassword())
                .trim()
                .isEmpty()) {

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

        if (!rbHombre.isSelected()
                && !rbMujer.isSelected()
                && !rbOtro.isSelected()) {

            errGeneroReg.setVisible(true);
            valid = false;

        }

        return valid;

    }

    /* =========================================================
     * BOTÓN
     * ========================================================= */

    /**
     * Aplica estilos al botón principal.
     *
     * @param button botón a estilizar
     */
    private void styleRegisterButton(JButton button) {

        button.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        16
                )
        );

        button.setForeground(Color.WHITE);

        button.setBackground(UiTheme.FOREST_GREEN);

        button.setFocusPainted(false);

        button.setBorderPainted(false);

        button.setCursor(
                new Cursor(Cursor.HAND_CURSOR)
        );

        button.setPreferredSize(BUTTON_SIZE);

        /*
         * Efecto hover
         */
        button.addMouseListener(
                new MouseAdapter() {

                    @Override
                    public void mouseEntered(MouseEvent e) {

                        button.setBackground(
                                UiTheme.FOREST_GREEN_HOVER
                        );

                    }

                    @Override
                    public void mouseExited(MouseEvent e) {

                        button.setBackground(
                                UiTheme.FOREST_GREEN
                        );

                    }
                }
        );

    }

    /* =========================================================
     * REGISTRO
     * ========================================================= */

    /**
     * Ejecuta el proceso de registro.
     */
    private void register() {

        try {

            clearRegisterErrors();

            if (!validateRegisterRequiredFields()) {

                JOptionPane.showMessageDialog(
                        this,
                        "Faltan campos obligatorios."
                );

                return;

            }

            User user = buildUserFromForm();

            boolean success =
                    controller.register(user);

            if (success) {

                JOptionPane.showMessageDialog(
                        this,
                        "Usuario registrado exitosamente."
                );

                if (onLoginSuccess != null) {

                    onLoginSuccess.accept(user);

                }

                dispose();

            } else {

                JOptionPane.showMessageDialog(
                        this,
                        "Error al registrar usuario."
                );

            }

        } catch (IllegalArgumentException e) {

            JOptionPane.showMessageDialog(
                    this,
                    e.getMessage(),
                    "Datos inválidos",
                    JOptionPane.WARNING_MESSAGE
            );

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    this,
                    e.getMessage(),
                    "Error Base de Datos",
                    JOptionPane.ERROR_MESSAGE
            );

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Error inesperado: " + e.getMessage(),
                    "Error crítico",
                    JOptionPane.ERROR_MESSAGE
            );

        }

    }

    /* =========================================================
     * CREACIÓN USER
     * ========================================================= */

    /**
     * Construye objeto User desde el formulario.
     *
     * @return usuario construido
     */
    private User buildUserFromForm() {

        User user = new User();

        user.setEmail(txtEmail.getText());

        user.setPassword(
                new String(txtPassword.getPassword())
        );

        user.setNombre(txtNombre.getText());

        user.setApellido(txtApellido.getText());

        user.setFechaNacimiento(
                getFechaNacimiento()
        );

        user.setRolId(resolveSelectedRoleId());

        /*
         * Conversión de género.
         */
        if (rbHombre.isSelected()) {

            user.setGenero("M");

        } else if (rbMujer.isSelected()) {

            user.setGenero("F");

        } else {

            user.setGenero("O");

        }

        return user;

    }

    private int resolveSelectedRoleId() {
        String selectedRole = String.valueOf(rolBox.getSelectedItem());
        return switch (selectedRole) {
            case "Administrador" -> 1;
            case "Supervisor" -> 2;
            case "Médico" -> 4;
            case "Recepcionista" -> 3;
            default -> 3;
        };
    }

    /* =========================================================
     * FECHA
     * ========================================================= */

    /**
     * Obtiene fecha seleccionada.
     *
     * @return fecha nacimiento
     */
    private LocalDate getFechaNacimiento() {

        try {

            int dia =
                    Integer.parseInt(
                            (String) cbDia.getSelectedItem()
                    );

            int anio =
                    Integer.parseInt(
                            (String) cbAnio.getSelectedItem()
                    );

            int mes =
                    convertMonth(
                            (String) cbMes.getSelectedItem()
                    );

            return LocalDate.of(anio, mes, dia);

        } catch (
                DateTimeException
                | NumberFormatException
                | NullPointerException e
        ) {

            throw new IllegalArgumentException(
                    "Fecha inválida."
            );

        }

    }

    /**
     * Convierte texto de mes a número.
     *
     * @param mesTexto texto del mes
     * @return número mes
     */
    private int convertMonth(String mesTexto) {

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

            default ->
                    throw new IllegalArgumentException(
                            "Mes inválido."
                    );
        };

    }

    /* =========================================================
     * DATOS COMBOBOX
     * ========================================================= */

    /**
     * Genera arreglo de días.
     *
     * @return días
     */
    private String[] generateDays() {

        String[] days = new String[31];

        for (int i = 0; i < 31; i++) {

            days[i] = String.valueOf(i + 1);

        }

        return days;

    }

    /**
     * Genera arreglo de años.
     *
     * @return años
     */
    private String[] generateYears() {

        String[] years = new String[100];

        int currentYear =
                Year.now().getValue();

        for (int i = 0; i < 100; i++) {

            years[i] =
                    String.valueOf(currentYear - i);

        }

        return years;

    }

}
