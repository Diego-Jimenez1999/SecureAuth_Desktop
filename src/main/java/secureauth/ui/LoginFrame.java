/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package secureauth.ui;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.sql.SQLException;
import secureauth.controller.AuthController;

/**
 * Ventana principal de autenticación que integra paneles de Login y Registro.
 * <p>
 * Esta clase implementa una interfaz moderna basada en la Imagen 1 del usuario,
 * utilizando GridBagLayout para un diseño responsivo y efectos visuales de foco.
 * </p>
 * * @author Diego Alexander Gaviria Jimenez
 * @version 2.2
 */
public class LoginFrame extends JFrame {

    //controlador del ui
    private final AuthController controller = new AuthController();
    /** Campo de texto para el correo en la sección de inicio de sesión. */
    private JTextField txtEmailLogin;
    /** Campo de contraseña para el inicio de sesión. */
    private JPasswordField txtPasswordLogin;
    /** Botón para ejecutar la acción de ingreso. */
    private JButton btnLogin;

    /** Campo de texto para el correo en el formulario de registro. */
    private JTextField txtEmail;
    /** Campo de contraseña para el nuevo usuario. */
    private JPasswordField txtPassword;
    /** Campo para el nombre del usuario. */
    private JTextField txtNombre;
    /** Campo para el apellido del usuario. */
    private JTextField txtApellido;
    
    /** Selector para el día de nacimiento. */
    private JComboBox<String> cbDia;
    /** Selector para el mes de nacimiento. */
    private JComboBox<String> cbMes;
    /** Selector para el año de nacimiento. */
    private JComboBox<String> cbAnio;
    
    /** Opción de género masculino. */
    private JRadioButton rbHombre;
    /** Opción de género femenino. */
    private JRadioButton rbMujer;
    /** Opción de género otro. */
    private JRadioButton rbOtro;
    /** Botón para finalizar el registro. */
    private JButton btnRegistrar;

    /**
     * Constructor de la clase LoginFrame.
     * Inicializa los componentes visuales y configura las propiedades de la ventana.
     */
    public LoginFrame() {
        initComponents();
        setupFrame();
    }

    /**
     * Inicializa y configura todos los componentes de la interfaz de usuario.
     * Define los grupos de botones y los layouts principales.
     */
    private void initComponents() {
        // Inicialización Login
        txtEmailLogin = new JTextField(15);
        txtPasswordLogin = new JPasswordField(15);
        btnLogin = new JButton("Iniciar");
        
        txtEmailLogin.setPreferredSize(new Dimension(150, 25));
        txtPasswordLogin.setPreferredSize(new Dimension(150, 25));

        // Inicialización Registro
        txtEmail = new JTextField();
        txtPassword = new JPasswordField();
        txtNombre = new JTextField();
        txtApellido = new JTextField();
        
        

        cbDia = new JComboBox<>(generarDias());
        cbMes = new JComboBox<>(new String[]{"Ene","Feb","Mar","Abr","May","Jun","Jul","Ago","Sep","Oct","Nov","Dic"});
        cbAnio = new JComboBox<>(generarAnios());

        rbHombre = new JRadioButton("Hombre");
        rbMujer = new JRadioButton("Mujer");
        rbOtro = new JRadioButton("Otro");
        
        ButtonGroup grupoGenero = new ButtonGroup();
        grupoGenero.add(rbHombre); grupoGenero.add(rbMujer); grupoGenero.add(rbOtro);

        btnRegistrar = new JButton("Registrarce");

        // Configuración de Layout Principal
        setLayout(new BorderLayout());
        add(buildHeader(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
        
        btnLogin.addActionListener(e -> login());
        btnRegistrar.addActionListener(e -> register());
    }
    
    
    /**
     * Método encargado de procesar el inicio de sesión.
     * Se ejecuta cuando el usuario presiona el botón "Iniciar".
    */
    private void login() {

        try {
            // =========================
            // 1. OBTENER DATOS (UI)
            // =========================
            String email = txtEmailLogin.getText();
            String password = new String(txtPasswordLogin.getPassword());

            // =========================
            // 2. VALIDACIÓN BÁSICA (UI)
            // =========================
            if (email.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Ingrese correo y contraseña");
                return;
            }

            // =========================
            // 3. LLAMADA AL CONTROLLER
            // =========================
            
            var user = controller.login(email, password);
            
          

            // =========================
            // 4. RESULTADO
            // =========================
            if (user != null) {
                
                JOptionPane.showMessageDialog(this, "Login exitoso");
                new IngresoFrame(user).setVisible(true);
                

                // 👉 Aquí luego abriremos el ingreso
                // new IngresoFrame().setVisible(true);

                this.dispose();

            } else {
                JOptionPane.showMessageDialog(this, "Correo o contraseña incorrectos");
            }

        } catch (HeadlessException | SQLException e) {
            JOptionPane.showMessageDialog(this, "Error en el sistema");
            e.printStackTrace();
        }
   }
    
 /**
 * Método encargado de registrar un nuevo usuario.
 */
private void register() {

    try {
        // =========================
        // 1. CREAR OBJETO USER
        // =========================
        secureauth.model.User user = new secureauth.model.User();

        user.setEmail(txtEmail.getText());
        user.setPassword(new String(txtPassword.getPassword()));
        user.setNombre(txtNombre.getText());
        user.setApellido(txtApellido.getText());

        // ⚠️ temporal (luego lo mejoramos)
        user.setFechaNacimiento(getFechaNacimiento());

        // Género
        if (rbHombre.isSelected()) user.setGenero("M");
        else if (rbMujer.isSelected()) user.setGenero("F");
        else user.setGenero("O");

        // =========================
        // 2. LLAMAR CONTROLLER
        // =========================
        boolean success = controller.register(user);

        // =========================
        // 3. RESULTADO
        // =========================
        if (success) {
            JOptionPane.showMessageDialog(this, "Usuario registrado");
            new IngresoFrame(user).setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Error al registrar");
        }

    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, e.getMessage());
    }
}
    
    

    /**
     * Construye el panel superior (Header) que contiene el título y el acceso rápido.
     * * @return Un JPanel configurado con el título profesional y campos de login.
     */
    private JPanel buildHeader() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        panel.setBackground(Color.WHITE);

        JLabel title = new JLabel("ING Diego Jiménez");
        title.setFont(new Font("Arial", Font.BOLD, 22));

        panel.add(title);
        panel.add(Box.createHorizontalStrut(80));
        panel.add(txtEmailLogin);
        panel.add(txtPasswordLogin);
        panel.add(btnLogin);
        
        applyCustomStyle(txtEmailLogin);
        applyCustomStyle(txtPasswordLogin);

        return panel;
    }

    /**
     * Construye el contenedor central con una distribución de dos paneles principales.
     * * @return Un JPanel con GridBagLayout que separa el logo del formulario de registro.
     */
    private JPanel buildCenter() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(new Color(210, 210, 210)); 

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        // Panel Logo
        gbc.gridx = 0; gbc.weightx = 0.55;
        gbc.insets = new Insets(20, 40, 20, 10);
        mainPanel.add(buildImagePanel(), gbc);

        // Panel Registro
        gbc.gridx = 1; gbc.weightx = 0.45;
        gbc.insets = new Insets(30, 10, 30, 60);
        mainPanel.add(buildRegisterPanel(), gbc);

        return mainPanel;
    }

    /**
     * Crea el panel que sostiene la imagen representativa (Logo del Lobo).
     * * @return JPanel con la imagen centrada.
     */
    private JPanel buildImagePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        JLabel image = new JLabel();
        image.setHorizontalAlignment(JLabel.CENTER);
        
        try {
            image.setIcon(new ImageIcon(getClass().getResource("/imagen.png")));
        } catch (Exception e) {
            image.setText("LOGO ING DIEGO");
        }

        panel.add(image, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Construye el panel de registro con separadores de colores y alineación vertical precisa.
     * Se han ajustado los tamaños y las restricciones para que ComboBoxes y RadioButtons 
     * se alineen con los bordes de los campos de texto superiores.
     * * @return JPanel configurado según la imagen de referencia.
     */
    private JPanel buildRegisterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(230, 230, 230)); // Fondo gris claro de la imagen
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // --- TÍTULO ---
        JLabel lblTitulo = new JLabel("Registrarse", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Serif", Font.BOLD, 45)); // Fuente tipo Serif como la imagen
        lblTitulo.setForeground(new Color(51, 51, 51));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 30, 0);
        panel.add(lblTitulo, gbc);

        // --- SECCIÓN 1: CORREO Y CONTRASEÑA ---
        
        gbc.gridwidth = 1; gbc.insets = new Insets(5, 5, 2, 5);
        gbc.gridy = 1; gbc.gridx = 0; panel.add(new JLabel("correo electroico"), gbc);
        gbc.gridx = 1; panel.add(new JLabel("contraseña"), gbc);

        gbc.gridy = 2; 
        gbc.gridx = 0; panel.add(txtEmail, gbc);
        gbc.gridx = 1; panel.add(txtPassword, gbc);
        applyCustomStyle(txtEmail); applyCustomStyle(txtPassword);

        // Separador 1
        addSeparator(panel, gbc, 3);

        // --- SECCIÓN 2: NOMBRE Y APELLIDO ---
        gbc.gridy = 4; gbc.gridx = 0; panel.add(new JLabel("Nombre"), gbc);
        gbc.gridx = 1; panel.add(new JLabel("Apellido"), gbc);

        gbc.gridy = 5;
        gbc.gridx = 0; panel.add(txtNombre, gbc);
        gbc.gridx = 1; panel.add(txtApellido, gbc);
        applyCustomStyle(txtNombre); applyCustomStyle(txtApellido);

        // Separador 2
        addSeparator(panel, gbc, 6);

        // --- SECCIÓN 3: FECHA DE NACIMIENTO ---
        gbc.gridy = 7; gbc.gridx = 0;
        panel.add(new JLabel("Fecha de nacimiento"), gbc);

        // Panel para ComboBoxes alineados
        JPanel fechaPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        fechaPanel.setOpaque(false);
        fechaPanel.add(createLabeledCombo(cbDia, "DD"));
        fechaPanel.add(createLabeledCombo(cbMes, "MM"));
        fechaPanel.add(createLabeledCombo(cbAnio, "AAA"));

        gbc.gridy = 7; gbc.gridx = 1; // Se coloca a la derecha del label "Fecha de nacimiento"
        panel.add(fechaPanel, gbc);

        // Aplicar foco a combos
        applyFocusAction(cbDia); applyFocusAction(cbMes); applyFocusAction(cbAnio);

        // Separador 3
        addSeparator(panel, gbc, 8);

        // --- SECCIÓN 4: GÉNERO ---
        gbc.gridy = 9; gbc.gridx = 0; gbc.gridwidth = 2;
        panel.add(new JLabel("Genero"), gbc);

        JPanel generoPanel = new JPanel(new GridLayout(1, 3, 5, 0));
        generoPanel.setOpaque(false);
        generoPanel.add(rbHombre); generoPanel.add(rbMujer); generoPanel.add(rbOtro);

        gbc.gridy = 10;
        panel.add(generoPanel, gbc);

        // Aplicar foco a radio buttons
        applyFocusAction(rbHombre); applyFocusAction(rbMujer); applyFocusAction(rbOtro);

        // Separador 4
        addSeparator(panel, gbc, 11);

        // --- BOTÓN REGISTRARSE ---
        btnRegistrar.setBackground(new Color(0, 153, 51));
        btnRegistrar.setForeground(new Color(51, 51, 51));
        btnRegistrar.setFont(new Font("Arial", Font.BOLD, 22));
        btnRegistrar.setBorder(BorderFactory.createLineBorder(new Color(0, 102, 0), 2));
        btnRegistrar.setPreferredSize(new Dimension(180, 50));

        // Efecto foco al botón
        applyFocusAction(btnRegistrar);

        gbc.gridy = 12; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(20, 0, 0, 0);
        panel.add(btnRegistrar, gbc);

        return panel;
    }

    /**
     * Agrega un JSeparator coloreado al panel.
     * @param p Panel destino.
     * @param g Restricciones actuales.
     * @param y Fila donde se colocará.
     */
    private void addSeparator(JPanel p, GridBagConstraints g, int y) {
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(204, 102, 0)); // Color naranja/rojo de la imagen
        sep.setPreferredSize(new Dimension(1, 5));
        int oldWidth = g.gridwidth;
        Insets oldInsets = g.insets;

        g.gridy = y; g.gridx = 0; g.gridwidth = 2;
        g.insets = new Insets(10, 0, 10, 0);
        p.add(sep, g);

        g.gridwidth = oldWidth; g.insets = oldInsets; // Restaurar
    }

    /**
     * Crea un contenedor para el combo con su etiqueta (DD, MM, AAA) debajo.
     */
    private JPanel createLabeledCombo(JComboBox<?> cb, String text) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(cb, BorderLayout.CENTER);
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setFont(new Font("Arial", Font.PLAIN, 10));
        p.add(lbl, BorderLayout.SOUTH);
        return p;
    }

    /**
     * Agrega un FocusListener genérico para componentes que no son de texto (Botones, Combos, Radios).
     * @param comp Componente a aplicar el efecto.
     */
    private void applyFocusAction(JComponent comp) {
        comp.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                comp.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                comp.setBorder(UIManager.getBorder("TextField.border"));
            }
        });
    }

    /**
     * Aplica el estilo personalizado de foco a un componente de texto.
     * <p>
     * Cuando el componente gana el foco, el fondo se torna rojizo y el borde 
     * se resalta en color rojo.
     * </p>
     * * @param field El JTextField o JPasswordField al que se le aplicará el estilo.
     */
    private void applyCustomStyle(JTextField field) {
        field.setPreferredSize(new Dimension(150, 30));
        field.setBorder(new LineBorder(Color.LIGHT_GRAY, 1));
        
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBackground(new Color(255, 235, 235)); // Relleno rojizo suave
                field.setBorder(new LineBorder(Color.RED, 2)); // Borde rojo grueso
            }

            @Override
            public void focusLost(FocusEvent e) {
                field.setBackground(Color.WHITE);
                field.setBorder(new LineBorder(Color.LIGHT_GRAY, 1));
            }
        });
    }

    /**
     * Genera un arreglo de Strings con los días del mes (1-31).
     * * @return Arreglo de strings con los días.
     */
    private String[] generarDias() {
        String[] dias = new String[31];
        for (int i = 0; i < 31; i++) dias[i] = String.valueOf(i + 1);
        return dias;
    }

    /**
     * Genera un arreglo de Strings con años de forma descendente empezando desde 2026.
     * * @return Arreglo de strings con los años.
     */
    private String[] generarAnios() {
        String[] anios = new String[100];
        int startYear = java.time.Year.now().getValue();
        for (int i = 0; i < 100; i++) anios[i] = String.valueOf(startYear - i);
        return anios;
    }

    /**
     * Convierte el mes en texto (Ene, Feb, etc.) a número.
     *
     * @param mesTexto mes en formato texto
     * @return número del mes (1-12)
    */
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
    
    
    
    
    /**
     * Obtiene la fecha de nacimiento seleccionada en los ComboBox
     * y la convierte a un objeto LocalDate.
     *
     * @return fecha de nacimiento como LocalDate
     * @throws IllegalArgumentException si la fecha no es válida
    */
    private java.time.LocalDate getFechaNacimiento() {

        try {
            // =========================
            // 1. OBTENER VALORES
            // =========================
            int dia = Integer.parseInt((String) cbDia.getSelectedItem());
            String mesTexto = (String) cbMes.getSelectedItem();
            int anio = Integer.parseInt((String) cbAnio.getSelectedItem());

            // =========================
            // 2. CONVERTIR MES
            // =========================
            int mes = convertirMes(mesTexto);

            // =========================
            // 3. CREAR FECHA
            // =========================
            return java.time.LocalDate.of(anio, mes, dia);

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Fecha de nacimiento inválida");
        }
    }
    
    
    
    
    
    

    /**
     * Configura los parámetros básicos del Frame (Título, Tamaño, Posición).
     */
    private void setupFrame() {
        setTitle("SecureAuth - Registro de Usuarios");
        setSize(1150, 750);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

  
}
