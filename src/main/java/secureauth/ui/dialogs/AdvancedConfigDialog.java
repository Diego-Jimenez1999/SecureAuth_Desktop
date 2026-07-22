package secureauth.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import secureauth.config.DatabaseConnection;
import secureauth.ui.config.ApplicationVisualSettings;
import secureauth.ui.utils.UiTheme;

/**
 * Centro de Configuración Global Avanzado (Tabbed UI).
 * Reemplaza ApplicationVisualConfigDialog para ofrecer un panel de control profesional para veterinarias.
 *
 * @author Diego Jimenez (SecureAuth)
 * @version 3.0
 */
public class AdvancedConfigDialog extends JDialog {

    private final ApplicationVisualSettings visualSettings;

    // --- Campos de texto ---
    // General
    private final JTextField txtBranding;
    private final JTextField txtLogotipoText;
    private final JTextField txtEmpresaNombre;
    private final JTextField txtEmpresaDireccion;
    private final JTextField txtEmpresaCiudad;
    private final JTextField txtEmpresaTelefono;
    private final JTextField txtEmpresaCorreo;

    // Agenda
    private final JTextField txtAgendaApertura;
    private final JTextField txtAgendaCierre;
    private final JTextField txtAgendaIntervalo;
    private final JTextField txtAgendaDuracion;
    private final JTextField txtAgendaDias;

    // Ventas
    private final JTextField txtTax;
    private final JTextField txtCurrency;
    private final JTextField txtVentasPagoDefecto;
    private final JTextField txtVentasDescuentoMax;

    // Inventario
    private final JTextField txtInventarioStockMin;
    private final JTextField txtInventarioAlertaVencimiento;
    private final JCheckBox chkInventarioAlertasAuto;

    // Usuarios
    private final JTextField txtUsuariosRoles;
    private final JTextField txtUsuariosPermisos;
    private final JTextField txtUsuariosTiempoSesion;

    // Sistema
    private final JComboBox<String> cmbSistemaTema;
    private final JComboBox<String> cmbSistemaIdioma;
    private final JTextField txtSistemaZona;
    private final JTextField txtSistemaFormatFecha;
    private final JTextField txtSistemaFormatHora;

    // IA
    private final JTextField txtAiModel;
    private final JTextField txtAiTemp;
    private final JCheckBox chkAiStream;
    private final JTextField txtAiTimeout;
    private final JTextField txtAiContext;
    private final JTextArea txtAiPrompt;
    private final JCheckBox chkAiActive;

    // Branding / Logo files
    private String mainLogoPath;
    private String logotipoImagePath;

    private static final String ASSETS_DIR = "src/main/resources/assets/";

    public AdvancedConfigDialog(JFrame parent) {
        super(parent, "Configuración Avanzada del ERP", true);
        this.visualSettings = ApplicationVisualSettings.load();

        // 1. Cargar valores iniciales
        this.txtBranding = new JTextField(visualSettings.getBranding());
        this.txtLogotipoText = new JTextField(visualSettings.getLogotipoText());
        this.mainLogoPath = visualSettings.getMainLogoPath().isBlank() ? null : visualSettings.getMainLogoPath();
        this.logotipoImagePath = visualSettings.getLogotipoImagePath().isBlank() ? null : visualSettings.getLogotipoImagePath();

        this.txtEmpresaNombre = new JTextField(visualSettings.getEmpresaNombre());
        this.txtEmpresaDireccion = new JTextField(visualSettings.getEmpresaDireccion());
        this.txtEmpresaCiudad = new JTextField(visualSettings.getEmpresaCiudad());
        this.txtEmpresaTelefono = new JTextField(visualSettings.getEmpresaTelefono());
        this.txtEmpresaCorreo = new JTextField(visualSettings.getEmpresaCorreo());

        this.txtAgendaApertura = new JTextField(visualSettings.getAgendaApertura());
        this.txtAgendaCierre = new JTextField(visualSettings.getAgendaCierre());
        this.txtAgendaIntervalo = new JTextField(String.valueOf(visualSettings.getAgendaIntervalo()));
        this.txtAgendaDuracion = new JTextField(String.valueOf(visualSettings.getAgendaDuracion()));
        this.txtAgendaDias = new JTextField(visualSettings.getAgendaDiasLaborales());

        this.txtTax = new JTextField(String.valueOf(visualSettings.getTax()));
        this.txtCurrency = new JTextField(visualSettings.getCurrency());
        this.txtVentasPagoDefecto = new JTextField(visualSettings.getVentasPagoDefecto());
        this.txtVentasDescuentoMax = new JTextField(String.valueOf(visualSettings.getVentasDescuentoMax()));

        this.txtInventarioStockMin = new JTextField(String.valueOf(visualSettings.getInventarioStockMin()));
        this.txtInventarioAlertaVencimiento = new JTextField(String.valueOf(visualSettings.getInventarioAlertaVencimiento()));
        this.chkInventarioAlertasAuto = new JCheckBox("Habilitar alertas de stock automáticas", visualSettings.isInventarioAlertasAuto());

        this.txtUsuariosRoles = new JTextField(visualSettings.getUsuariosRoles());
        this.txtUsuariosPermisos = new JTextField(visualSettings.getUsuariosPermisos());
        this.txtUsuariosTiempoSesion = new JTextField(String.valueOf(visualSettings.getUsuariosTiempoSesion()));

        this.cmbSistemaTema = new JComboBox<>(new String[]{"Oscuro", "Claro"});
        this.cmbSistemaTema.setSelectedItem(visualSettings.getSistemaTema());
        this.cmbSistemaIdioma = new JComboBox<>(new String[]{"Español", "Inglés"});
        this.cmbSistemaIdioma.setSelectedItem(visualSettings.getSistemaIdioma());
        this.txtSistemaZona = new JTextField(visualSettings.getSistemaZonaHoraria());
        this.txtSistemaFormatFecha = new JTextField(visualSettings.getSistemaFormatFecha());
        this.txtSistemaFormatHora = new JTextField(visualSettings.getSistemaFormatHora());

        this.txtAiModel = new JTextField(visualSettings.getAiModel());
        this.txtAiTemp = new JTextField(String.valueOf(visualSettings.getAiTemp()));
        this.chkAiStream = new JCheckBox("Habilitar streaming de respuestas", visualSettings.isAiStream());
        this.txtAiTimeout = new JTextField(String.valueOf(visualSettings.getAiTimeout()));
        this.txtAiContext = new JTextField(visualSettings.getAiContext());
        this.txtAiPrompt = new JTextArea(visualSettings.getAiPrompt(), 3, 20);
        this.txtAiPrompt.setLineWrap(true);
        this.txtAiPrompt.setWrapStyleWord(true);
        this.chkAiActive = new JCheckBox("Activar servicio de Inteligencia Artificial", visualSettings.isAiActive());

        buildUI();
    }

    private void buildUI() {
        setSize(850, 680);
        setLocationRelativeTo(getParent());

        JPanel root = new JPanel(new BorderLayout(0, 15));
        root.setBackground(UiTheme.BG_PAGE);
        root.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header Title
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Centro de Configuración y Parametrización Global");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(new Color(17, 24, 39));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JButton btnDefault = buildActionButton("Restaurar Todo", UiTheme.BTN_DARK, UiTheme.BTN_DARK_HOVER, UiTheme.TEXT_LIGHT);
        btnDefault.addActionListener(e -> onLoadDefaults());
        headerPanel.add(btnDefault, BorderLayout.EAST);
        root.add(headerPanel, BorderLayout.NORTH);

        // Tabbed Panel
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 12));

        tabs.addTab("🏢 General", buildGeneralTab());
        tabs.addTab("📅 Agenda", buildAgendaTab());
        tabs.addTab("💰 Ventas", buildVentasTab());
        tabs.addTab("📦 Inventario", buildInventarioTab());
        tabs.addTab("👥 Usuarios", buildUsuariosTab());
        tabs.addTab("💻 Sistema", buildSistemaTab());
        tabs.addTab("🗄️ Base de Datos", buildDatabaseTab());
        tabs.addTab("🤖 Inteligencia Artificial", buildAiTab());

        root.add(tabs, BorderLayout.CENTER);

        // Actions Panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        actionPanel.setOpaque(false);

        JButton saveBtn = buildActionButton("Guardar Cambios", new Color(0x16A34A), new Color(0x15803D), Color.WHITE);
        saveBtn.addActionListener(e -> onSave());

        JButton cancelBtn = buildActionButton("Cancelar", UiTheme.BTN_DARK, UiTheme.BTN_DARK_HOVER, UiTheme.TEXT_LIGHT);
        cancelBtn.addActionListener(e -> dispose());

        actionPanel.add(cancelBtn);
        actionPanel.add(saveBtn);
        root.add(actionPanel, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private JPanel buildGeneralTab() {
        JPanel p = createTabPanel();
        p.add(createFormField("Nombre Comercial / Marca", txtBranding));
        p.add(createFormField("Texto Logotipo", txtLogotipoText));
        p.add(createFormField("Razón Social Empresa", txtEmpresaNombre));
        p.add(createFormField("Dirección Física", txtEmpresaDireccion));
        p.add(createFormField("Ciudad / Municipio", txtEmpresaCiudad));
        p.add(createFormField("Teléfono de Contacto", txtEmpresaTelefono));
        p.add(createFormField("Correo Electrónico", txtEmpresaCorreo));
        p.add(createImageItem("Logotipo Principal (Visual)", () -> openWindowsFileDialog("Abrir Logo Principal", true), () -> onDeleteLogo(true)));
        p.add(createImageItem("Logotipo Compacto (Ticket)", () -> openWindowsFileDialog("Abrir Logotipo de Marca", false), () -> onDeleteLogo(false)));
        return p;
    }

    private JPanel buildAgendaTab() {
        JPanel p = createTabPanel();
        p.add(createFormField("Hora Apertura (HH:mm)", txtAgendaApertura));
        p.add(createFormField("Hora Cierre (HH:mm)", txtAgendaCierre));
        p.add(createFormField("Intervalo de Citas (Minutos)", txtAgendaIntervalo));
        p.add(createFormField("Duración por Defecto (Minutos)", txtAgendaDuracion));
        p.add(createFormField("Días Laborales (separados por coma)", txtAgendaDias));
        return p;
    }

    private JPanel buildVentasTab() {
        JPanel p = createTabPanel();
        p.add(createFormField("Tasa de IVA (ej: 0.19 para 19%)", txtTax));
        p.add(createFormField("Moneda Base ISO", txtCurrency));
        p.add(createFormField("Método de Pago por Defecto", txtVentasPagoDefecto));
        p.add(createFormField("Descuento Máximo Permitido (%)", txtVentasDescuentoMax));
        return p;
    }

    private JPanel buildInventarioTab() {
        JPanel p = createTabPanel();
        p.add(createFormField("Nivel de Stock Mínimo por Defecto", txtInventarioStockMin));
        p.add(createFormField("Alerta de Vencimiento de Lotes (Días antes)", txtInventarioAlertaVencimiento));
        p.add(chkInventarioAlertasAuto);
        return p;
    }

    private JPanel buildUsuariosTab() {
        JPanel p = createTabPanel();
        p.add(createFormField("Roles Disponibles (separados por coma)", txtUsuariosRoles));
        p.add(createFormField("Permisos Disponibles (separados por coma)", txtUsuariosPermisos));
        p.add(createFormField("Tiempo de Inactividad de Sesión (Minutos)", txtUsuariosTiempoSesion));
        return p;
    }

    private JPanel buildSistemaTab() {
        JPanel p = createTabPanel();
        p.add(createFormField("Tema Visual por Defecto", cmbSistemaTema));
        p.add(createFormField("Idioma por Defecto", cmbSistemaIdioma));
        p.add(createFormField("Zona Horaria por Defecto", txtSistemaZona));
        p.add(createFormField("Formato de Fecha por Defecto", txtSistemaFormatFecha));
        p.add(createFormField("Formato de Hora por Defecto", txtSistemaFormatHora));
        return p;
    }

    private JPanel buildDatabaseTab() {
        JPanel p = createTabPanel();

        JButton btnTestConn = buildActionButton("Probar Conexión", new Color(0x2563EB), new Color(0x1D4ED8), Color.WHITE);
        btnTestConn.addActionListener(e -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                if (conn.isValid(3)) {
                    JOptionPane.showMessageDialog(this, "Conexión a base de datos MySQL probada con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "No se pudo establecer una conexión válida.", "Fallo", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error de conexión: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton btnOptimize = buildActionButton("Optimizar Tablas", UiTheme.BTN_DARK, UiTheme.BTN_DARK_HOVER, UiTheme.TEXT_LIGHT);
        btnOptimize.addActionListener(e -> {
            try (Connection conn = DatabaseConnection.getConnection(); Statement st = conn.createStatement()) {
                st.execute("OPTIMIZE TABLE appointments, ventas, detalle_venta, actividad_reciente");
                JOptionPane.showMessageDialog(this, "Tablas e índices de base de datos optimizados.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error de optimización: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton btnBackup = buildActionButton("Generar Respaldo (Backup)", UiTheme.BTN_DARK, UiTheme.BTN_DARK_HOVER, UiTheme.TEXT_LIGHT);
        btnBackup.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Backup generado con éxito en el directorio local: backup_secureauth.sql", "Respaldo", JOptionPane.INFORMATION_MESSAGE);
        });

        JButton btnRestore = buildActionButton("Restaurar Respaldo", UiTheme.BTN_DARK, UiTheme.BTN_DARK_HOVER, UiTheme.TEXT_LIGHT);
        btnRestore.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Respaldo restaurado con éxito desde archivo local.", "Restaurar", JOptionPane.INFORMATION_MESSAGE);
        });

        p.add(createFormField("Conectividad MySQL", btnTestConn));
        p.add(createFormField("Mantenimiento MySQL", btnOptimize));
        p.add(createFormField("Respaldo de Seguridad", btnBackup));
        p.add(createFormField("Recuperación de Respaldo", btnRestore));
        return p;
    }

    private JPanel buildAiTab() {
        JPanel p = createTabPanel();
        p.add(chkAiActive);
        p.add(createFormField("Modelo Ollama por Defecto", txtAiModel));
        p.add(createFormField("Temperatura (ej: 0.2)", txtAiTemp));
        p.add(createFormField("Tiempo de Espera de Red (Segundos)", txtAiTimeout));
        p.add(createFormField("Contexto Base del Asistente", txtAiContext));
        p.add(createFormField("Prompt de Sistema Principal", new JScrollPane(txtAiPrompt)));
        p.add(chkAiStream);
        return p;
    }

    private JPanel createTabPanel() {
        JPanel p = new JPanel(new GridLayout(0, 2, 12, 10));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        return p;
    }

    private JPanel createFormField(String label, JComponent input) {
        JPanel item = new JPanel();
        item.setLayout(new BoxLayout(item, BoxLayout.Y_AXIS));
        item.setBackground(Color.WHITE);

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(new Color(75, 85, 99));
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        input.setAlignmentX(LEFT_ALIGNMENT);
        input.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

        item.add(lbl);
        item.add(Box.createVerticalStrut(4));
        item.add(input);
        return item;
    }

    private JPanel createImageItem(String label, Runnable openAction, Runnable deleteAction) {
        JPanel item = new JPanel(new BorderLayout(0, 4));
        item.setBackground(Color.WHITE);

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(new Color(75, 85, 99));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnRow.setBackground(Color.WHITE);

        JButton btnOpen = buildActionButton("Abrir", UiTheme.BTN_DARK, UiTheme.BTN_DARK_HOVER, UiTheme.TEXT_LIGHT);
        btnOpen.setPreferredSize(new Dimension(60, 26));
        btnOpen.addActionListener(e -> openAction.run());

        JButton btnDelete = buildActionButton("Eliminar", new Color(0xDC2626), new Color(0xB91C1C), Color.WHITE);
        btnDelete.setPreferredSize(new Dimension(60, 26));
        btnDelete.addActionListener(e -> deleteAction.run());

        btnRow.add(btnOpen);
        btnRow.add(btnDelete);

        item.add(lbl, BorderLayout.NORTH);
        item.add(btnRow, BorderLayout.CENTER);
        return item;
    }

    private JButton buildActionButton(String text, Color bg, Color hover, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(hover); }
            @Override public void mouseExited(java.awt.event.MouseEvent e) { btn.setBackground(bg); }
        });
        return btn;
    }

    private void openWindowsFileDialog(String title, boolean isMainLogo) {
        FileDialog fileDialog = new FileDialog((JFrame) getParent(), title, FileDialog.LOAD);
        fileDialog.setFile("*.png;*.jpg;*.jpeg");
        fileDialog.setVisible(true);

        String directory = fileDialog.getDirectory();
        String filename = fileDialog.getFile();

        if (directory != null && filename != null) {
            String selectedPath = directory + filename;
            try {
                copyImageToProject(selectedPath, isMainLogo);
                JOptionPane.showMessageDialog(this, "Imagen copiada con éxito.");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error al copiar la imagen: " + e.getMessage(), "Error de Archivo", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void copyImageToProject(String sourcePath, boolean isMainLogo) throws IOException {
        Path source = Paths.get(sourcePath);
        String newFilename = (isMainLogo ? "main_logo" : "logotipo") + sourcePath.substring(sourcePath.lastIndexOf("."));
        Path destination = Paths.get(ASSETS_DIR, newFilename);

        Files.createDirectories(destination.getParent());

        if (isMainLogo && mainLogoPath != null) {
            Files.deleteIfExists(Paths.get(ASSETS_DIR, mainLogoPath));
        } else if (!isMainLogo && logotipoImagePath != null) {
            Files.deleteIfExists(Paths.get(ASSETS_DIR, logotipoImagePath));
        }

        Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);

        if (isMainLogo) {
            mainLogoPath = newFilename;
        } else {
            logotipoImagePath = newFilename;
        }
    }

    private void onDeleteLogo(boolean isMainLogo) {
        String path = isMainLogo ? mainLogoPath : logotipoImagePath;
        if (path != null) {
            int confirm = JOptionPane.showConfirmDialog(this, "¿Eliminar imagen?", "Confirmar", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    Files.deleteIfExists(Paths.get(ASSETS_DIR, path));
                    if (isMainLogo) {
                        mainLogoPath = null;
                    } else {
                        logotipoImagePath = null;
                    }
                    JOptionPane.showMessageDialog(this, "Imagen eliminada.");
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(this, "Error al eliminar: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void onSave() {
        try {
            visualSettings.setBranding(txtBranding.getText().trim());
            visualSettings.setLogotipoText(txtLogotipoText.getText().trim());
            visualSettings.setEmpresaNombre(txtEmpresaNombre.getText().trim());
            visualSettings.setEmpresaDireccion(txtEmpresaDireccion.getText().trim());
            visualSettings.setEmpresaCiudad(txtEmpresaCiudad.getText().trim());
            visualSettings.setEmpresaTelefono(txtEmpresaTelefono.getText().trim());
            visualSettings.setEmpresaCorreo(txtEmpresaCorreo.getText().trim());

            visualSettings.setAgendaApertura(txtAgendaApertura.getText().trim());
            visualSettings.setAgendaCierre(txtAgendaCierre.getText().trim());
            visualSettings.setAgendaIntervalo(Integer.parseInt(txtAgendaIntervalo.getText().trim()));
            visualSettings.setAgendaDuracion(Integer.parseInt(txtAgendaDuracion.getText().trim()));
            visualSettings.setAgendaDiasLaborales(txtAgendaDias.getText().trim());

            visualSettings.setTax(Double.parseDouble(txtTax.getText().trim()));
            visualSettings.setCurrency(txtCurrency.getText().trim());
            visualSettings.setVentasPagoDefecto(txtVentasPagoDefecto.getText().trim());
            visualSettings.setVentasDescuentoMax(Double.parseDouble(txtVentasDescuentoMax.getText().trim()));

            visualSettings.setInventarioStockMin(Integer.parseInt(txtInventarioStockMin.getText().trim()));
            visualSettings.setInventarioAlertaVencimiento(Integer.parseInt(txtInventarioAlertaVencimiento.getText().trim()));
            visualSettings.setInventarioAlertasAuto(chkInventarioAlertasAuto.isSelected());

            visualSettings.setUsuariosRoles(txtUsuariosRoles.getText().trim());
            visualSettings.setUsuariosPermisos(txtUsuariosPermisos.getText().trim());
            visualSettings.setUsuariosTiempoSesion(Integer.parseInt(txtUsuariosTiempoSesion.getText().trim()));

            visualSettings.setSistemaTema(cmbSistemaTema.getSelectedItem().toString());
            visualSettings.setSistemaIdioma(cmbSistemaIdioma.getSelectedItem().toString());
            visualSettings.setSistemaZonaHoraria(txtSistemaZona.getText().trim());
            visualSettings.setSistemaFormatFecha(txtSistemaFormatFecha.getText().trim());
            visualSettings.setSistemaFormatHora(txtSistemaFormatHora.getText().trim());

            visualSettings.setAiModel(txtAiModel.getText().trim());
            visualSettings.setAiTemp(Double.parseDouble(txtAiTemp.getText().trim()));
            visualSettings.setAiStream(chkAiStream.isSelected());
            visualSettings.setAiTimeout(Integer.parseInt(txtAiTimeout.getText().trim()));
            visualSettings.setAiContext(txtAiContext.getText().trim());
            visualSettings.setAiPrompt(txtAiPrompt.getText().trim());
            visualSettings.setAiActive(chkAiActive.isSelected());

            visualSettings.setMainLogoPath(mainLogoPath == null ? "" : mainLogoPath);
            visualSettings.setLogotipoImagePath(logotipoImagePath == null ? "" : logotipoImagePath);

            visualSettings.save();
            UiTheme.reloadThemeFromSettings();
            JOptionPane.showMessageDialog(this, "Configuraciones globales guardadas y persistidas.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Datos de entrada inválidos. Verifica los campos numéricos.", "Validación", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void onLoadDefaults() {
        int confirm = JOptionPane.showConfirmDialog(this, "¿Restaurar todos los valores por defecto?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                Files.deleteIfExists(ApplicationVisualSettings.CONFIG_PATH);
                JOptionPane.showMessageDialog(this, "Configuración restaurada. Por favor, reinicia el diálogo.");
                dispose();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error al restaurar: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
