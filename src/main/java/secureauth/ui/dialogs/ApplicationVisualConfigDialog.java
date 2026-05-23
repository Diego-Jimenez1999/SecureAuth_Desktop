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
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import secureauth.ui.config.ApplicationVisualSettings;
import secureauth.ui.sales.SalesModuleSettings;
import secureauth.ui.utils.JpanelR;
import secureauth.ui.utils.UiTheme;

/**
 * Diálogo avanzado para la edición visual completa de la aplicación,
 * incluyendo colores, logotipos y marca. Los cambios se persisten en un archivo TXT.
 * @author Diego Jimenez (SecureAuth)
 * @version 2.1
 */
public class ApplicationVisualConfigDialog extends JDialog {

    private final SalesModuleSettings settings;
    
    // Campos de Texto
    private final JTextField txtBranding;
    private final JTextField txtLogotipoText;
    private final JTextField txtTax;
    private final JTextField txtCurrency;
    private final JTextField txtFormat;
    private final JTextField txtSizes;
    private final JTextField txtPrimaryColor;
    private final JTextField txtSecondaryColor;
    private final JTextField txtTertiaryColor;
    private final JTextField txtSlogan;
    private final JTextField txtLoginTitle;
    private final JTextField txtLoginSubtitle;
    private ApplicationVisualSettings visualSettings;

    // Rutas de Imágenes (para persistencia)
    private String mainLogoPath;
    private String logotipoImagePath;

    // Colores Oficiales (según utils)
    private static final Color BG_CARD = Color.WHITE;
    private static final Color BORDER_COLOR = new Color(0xE5E7EB);
    private static final Color TEXT_DARK = new Color(0x111827);
    private static final Color TEXT_GRAY = new Color(0x6B7280);
    private static final Color SUCCESS_GREEN = new Color(0x16A34A);
    private static final Color DANGER_RED = new Color(0xDC2626);

    // Tipografía
    private static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 15);

    // Ruta de recursos del proyecto
    private static final String ASSETS_DIR = "src/main/resources/assets/";
    public ApplicationVisualConfigDialog(JFrame parent) {
        super(parent, "Configuración de Marca y Visual", true);
        this.settings = SalesModuleSettings.getInstance();
        
        loadConfigurationFromFile(); // Intentar cargar la configuración existente

        // Inicializar campos con los valores cargados o por defecto
        this.txtBranding = new JTextField(settings.getBrandingName());
        this.txtLogotipoText = new JTextField(settings.getLogotipoText());
        this.txtTax = new JTextField(String.valueOf(settings.getTaxRate()));
        this.txtCurrency = new JTextField(settings.getCurrency());
        this.txtFormat = new JTextField(settings.getPriceFormat());
        this.txtSizes = new JTextField(String.join(",", settings.getDefaultSizes()));
        this.txtPrimaryColor = new JTextField(visualSettings.getPrimaryColor());
        this.txtSecondaryColor = new JTextField(visualSettings.getSecondaryColor());
        this.txtTertiaryColor = new JTextField(visualSettings.getTertiaryColor());
        this.txtSlogan = new JTextField(visualSettings.getSlogan());
        this.txtLoginTitle = new JTextField(visualSettings.getLoginTitle());
        this.txtLoginSubtitle = new JTextField(visualSettings.getLoginSubtitle());
        
        init();
    }

    private void init() {
        setSize(760, 620);
        setLocationRelativeTo(getParent());

        JPanel root = new JPanel(new BorderLayout(0, 15));
        root.setBackground(UiTheme.BG_PAGE);
        root.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 1. Panel de Cabecera (Título y Botón de Por Defecto)
        root.add(buildHeaderSection(), BorderLayout.NORTH);

        // 2. Panel Central (Cards de Configuración)
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        centerPanel.add(buildBrandingCard());
        centerPanel.add(Box.createVerticalStrut(15));
        centerPanel.add(buildSalesParamsCard());
        centerPanel.add(Box.createVerticalStrut(15));
        centerPanel.add(buildColorsCard());

        root.add(centerPanel, BorderLayout.CENTER);

        // 3. Panel Inferior (Botones de Acción)
        root.add(buildActionPanel(), BorderLayout.SOUTH);

        setContentPane(root);
    }

    private JPanel buildHeaderSection() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JLabel title = new JLabel("Centro de Configuración Visual");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(TEXT_DARK);

        JButton btnDefault = buildActionButton("Cargar Datos por Defecto", UiTheme.BTN_DARK, UiTheme.BTN_DARK_HOVER, UiTheme.TEXT_LIGHT);
        btnDefault.setFont(FONT_BOLD);
        btnDefault.setPreferredSize(new Dimension(200, 34));
        btnDefault.addActionListener(e -> onLoadDefaults());

        header.add(title, BorderLayout.WEST);
        header.add(btnDefault, BorderLayout.EAST);
        return header;
    }

    private JpanelR buildBrandingCard() {
        JpanelR card = createCard();
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel sTitle = new JLabel("Branding y Logotipo");
        sTitle.setFont(FONT_TITLE);
        sTitle.setForeground(TEXT_DARK);
        card.add(sTitle, BorderLayout.NORTH);

        JPanel content = new JPanel(new GridLayout(2, 2, 10, 8));
        content.setBackground(BG_CARD);

        // Fila 1: Nombre y Logo Principal
        content.add(createFormField("Nombre del Negocio", txtBranding));
        content.add(createImageItem("Logo Principal", () -> openWindowsFileDialog("Abrir Logo Principal", true), () -> onDeleteLogo(true)));

        // Fila 2: Texto Logotipo e Imagen Logotipo
        content.add(createFormField("Texto Logotipo", txtLogotipoText));
        content.add(createImageItem("Logotipo", () -> openWindowsFileDialog("Abrir Logotipo de Marca", false), () -> onDeleteLogo(false)));

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JpanelR buildSalesParamsCard() {
        JpanelR card = createCard();
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel sTitle = new JLabel("Parámetros de Ventas");
        sTitle.setFont(FONT_TITLE);
        sTitle.setForeground(TEXT_DARK);
        card.add(sTitle, BorderLayout.NORTH);

        JPanel content = new JPanel(new GridLayout(2, 2, 10, 8));
        content.setBackground(BG_CARD);

        content.add(createFormField("Impuesto (ej: 0.19)", txtTax));
        content.add(createFormField("Moneda", txtCurrency));
        content.add(createFormField("Formato precios", txtFormat));
        content.add(createFormField("Tamaños por defecto (coma)", txtSizes));

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JpanelR buildColorsCard() {
        JpanelR card = createCard();
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel sTitle = new JLabel("Colores Principales");
        sTitle.setFont(FONT_TITLE);
        sTitle.setForeground(TEXT_DARK);
        card.add(sTitle, BorderLayout.NORTH);

        JPanel content = new JPanel(new GridLayout(3, 2, 10, 8));
        content.setBackground(BG_CARD);
        content.add(createFormField("Color primario (#RRGGBB)", txtPrimaryColor));
        content.add(createFormField("Color secundario (#RRGGBB)", txtSecondaryColor));
        content.add(createFormField("Color terciario (#RRGGBB)", txtTertiaryColor));
        content.add(createFormField("Slogan", txtSlogan));
        content.add(createFormField("Título login", txtLoginTitle));
        content.add(createFormField("Subtítulo login", txtLoginSubtitle));
        card.add(content, BorderLayout.CENTER);

        return card;
    }

    private JpanelR createCard() {
        JpanelR card = new JpanelR();
        card.setArc(14);
        card.setBackgroundColor(BG_CARD);
        card.setBorderConfig(BORDER_COLOR, 1.0f);
        return card;
    }

    private JPanel buildActionPanel() {
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        actions.setOpaque(false);
        actions.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JButton save = buildActionButton("Guardar Cambios", SUCCESS_GREEN, new Color(0x15803D), Color.WHITE);
        save.addActionListener(e -> onSave());

        JButton cancel = buildActionButton("Cancelar", UiTheme.BTN_DARK, UiTheme.BTN_DARK_HOVER, UiTheme.TEXT_LIGHT);
        cancel.addActionListener(e -> dispose());

        actions.add(cancel);
        actions.add(save);
        return actions;
    }

    // --- Helpers de UI ---

    private JPanel createFormField(String label, JComponent input) {
        JPanel item = new JPanel();
        item.setLayout(new BoxLayout(item, BoxLayout.Y_AXIS));
        item.setBackground(BG_CARD);
        item.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));

        JLabel lbl = new JLabel(label);
        lbl.setFont(FONT_BOLD);
        lbl.setForeground(TEXT_GRAY);
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        input.setAlignmentX(LEFT_ALIGNMENT);
        input.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

        item.add(lbl);
        item.add(Box.createVerticalStrut(3));
        item.add(input);
        return item;
    }

    private JPanel createImageItem(String label, Runnable openAction, Runnable deleteAction) {
        JPanel item = new JPanel(new BorderLayout(0, 4));
        item.setBackground(BG_CARD);
        
        JLabel lbl = new JLabel(label);
        lbl.setFont(FONT_BOLD);
        lbl.setForeground(TEXT_GRAY);
        
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnRow.setBackground(BG_CARD);
        
        JButton btnOpen = buildActionButton("Abrir", UiTheme.BTN_DARK, UiTheme.BTN_DARK_HOVER, UiTheme.TEXT_LIGHT);
        btnOpen.setPreferredSize(new Dimension(60, 26));
        btnOpen.addActionListener(e -> openAction.run());
        
        JButton btnDelete = buildActionButton("Eliminar", DANGER_RED, new Color(0xB91C1C), Color.WHITE);
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
        btn.setFont(FONT_BOLD);
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

    // --- Lógica de Manejo de Archivos (Módulo Ventas Mejorado) ---

    private void openWindowsFileDialog(String title, boolean isMainLogo) {
        // Usa java.awt.FileDialog para invocar el diálogo nativo de Windows
        FileDialog fileDialog = new FileDialog((JFrame) getParent(), title, FileDialog.LOAD);
        fileDialog.setFile("*.png;*.jpg;*.jpeg"); // Filtros de archivos
        fileDialog.setVisible(true);

        String directory = fileDialog.getDirectory();
        String filename = fileDialog.getFile();

        if (directory != null && filename != null) {
            String selectedPath = directory + filename;
            try {
                // Copiar la imagen al proyecto y eliminar la anterior
                copyImageToProject(selectedPath, isMainLogo);
                dispose(); // Cerrar para recargar las imágenes
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error al copiar la imagen: " + e.getMessage(), "Error de Archivo", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void copyImageToProject(String sourcePath, boolean isMainLogo) throws IOException {
        Path source = Paths.get(sourcePath);
        String newFilename = (isMainLogo ? "main_logo" : "logotipo") + sourcePath.substring(sourcePath.lastIndexOf("."));
        Path destination = Paths.get(ASSETS_DIR, newFilename);

        // Crear directorio de destino si no existe
        Files.createDirectories(destination.getParent());

        // Eliminar imagen anterior si existe
        if (isMainLogo && mainLogoPath != null) {
            Files.deleteIfExists(Paths.get(ASSETS_DIR, mainLogoPath));
        } else if (!isMainLogo && logotipoImagePath != null) {
            Files.deleteIfExists(Paths.get(ASSETS_DIR, logotipoImagePath));
        }

        // Copiar nueva imagen
        Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);

        // Guardar la nueva ruta relativa
        if (isMainLogo) {
            mainLogoPath = newFilename;
        } else {
            logotipoImagePath = newFilename;
        }
    }

    private void onDeleteLogo(boolean isMainLogo) {
        String path = isMainLogo ? mainLogoPath : logotipoImagePath;
        if (path != null) {
            int confirm = JOptionPane.showConfirmDialog(this, "¿Eliminar " + (isMainLogo ? "el logo principal?" : "el logotipo?") + "\nSe restaurará el predeterminado.", "Confirmar Eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    Files.deleteIfExists(Paths.get(ASSETS_DIR, path));
                    if (isMainLogo) {
                        mainLogoPath = null;
                    } else {
                        logotipoImagePath = null;
                    }
                    JOptionPane.showMessageDialog(this, "Imagen eliminada con éxito.", "Eliminado", JOptionPane.INFORMATION_MESSAGE);
                    dispose(); // Cerrar para recargar
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(this, "Error al eliminar: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "No hay imagen personalizada cargada.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // --- Persistencia en Archivo TXT (Mejorado) ---

    private void loadConfigurationFromFile() {
        this.visualSettings = ApplicationVisualSettings.load();
        this.mainLogoPath = visualSettings.getMainLogoPath().isBlank() ? null : visualSettings.getMainLogoPath();
        this.logotipoImagePath = visualSettings.getLogotipoImagePath().isBlank() ? null : visualSettings.getLogotipoImagePath();
        settings.update(visualSettings.getBranding(), visualSettings.getLogotipoText(), visualSettings.getTax(),
                visualSettings.getCurrency(), visualSettings.getFormat(), visualSettings.getSizes());
    }

    private void onSave() {
        try {
            List<String> sizes = Arrays.stream(txtSizes.getText().split(",")).map(String::trim).filter(s -> !s.isBlank()).toList();
            
            // 1. Actualizar Settings en memoria
            settings.update(txtBranding.getText().trim(), txtLogotipoText.getText().trim(), Double.parseDouble(txtTax.getText().trim()),
                    txtCurrency.getText().trim(), txtFormat.getText().trim(), sizes);
            
            // 2. Persistir en archivo TXT
            saveConfigurationToFile();
            
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Datos inválidos (verifique el impuesto y tamaños).", "Validación", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void saveConfigurationToFile() throws IOException {
        visualSettings.setBranding(txtBranding.getText().trim());
        visualSettings.setLogotipoText(txtLogotipoText.getText().trim());
        visualSettings.setPrimaryColor(txtPrimaryColor.getText().trim());
        visualSettings.setSecondaryColor(txtSecondaryColor.getText().trim());
        visualSettings.setTertiaryColor(txtTertiaryColor.getText().trim());
        visualSettings.setSlogan(txtSlogan.getText().trim());
        visualSettings.setLoginTitle(txtLoginTitle.getText().trim());
        visualSettings.setLoginSubtitle(txtLoginSubtitle.getText().trim());
        visualSettings.setTax(Double.parseDouble(txtTax.getText().trim()));
        visualSettings.setCurrency(txtCurrency.getText().trim());
        visualSettings.setFormat(txtFormat.getText().trim());
        visualSettings.setSizes(Arrays.stream(txtSizes.getText().split(",")).map(String::trim).filter(s -> !s.isBlank()).toList());
        visualSettings.setMainLogoPath(mainLogoPath == null ? "" : mainLogoPath);
        visualSettings.setLogotipoImagePath(logotipoImagePath == null ? "" : logotipoImagePath);
        visualSettings.save();
        UiTheme.reloadThemeFromSettings();
        SwingUtilities.updateComponentTreeUI(this);
        revalidate();
        repaint();
        JOptionPane.showMessageDialog(this, "Configuración guardada en archivo con éxito.", "Guardado", JOptionPane.INFORMATION_MESSAGE);
    }

    private void onLoadDefaults() {
        int confirm = JOptionPane.showConfirmDialog(this, "¿Cargar datos operativos por defecto de la veterinaria?\nEsto sobrescribirá sus cambios.", "Cargar Predeterminado", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // 1. Eliminar archivo de configuración personalizado
                Files.deleteIfExists(ApplicationVisualSettings.CONFIG_PATH);
                
                // 2. Eliminar imágenes personalizadas si existen
                if (mainLogoPath != null) Files.deleteIfExists(Paths.get(ASSETS_DIR, mainLogoPath));
                if (logotipoImagePath != null) Files.deleteIfExists(Paths.get(ASSETS_DIR, logotipoImagePath));

                // 3. Restaurar valores por defecto en Settings en memoria
                settings.loadDefaultSettings(); 
                visualSettings = new ApplicationVisualSettings();
                UiTheme.restoreDefaultTheme();
                SwingUtilities.updateComponentTreeUI(this);
                revalidate();
                repaint();
                
                JOptionPane.showMessageDialog(this, "Datos por defecto cargados con éxito.", "Restaurado", JOptionPane.INFORMATION_MESSAGE);
                dispose(); // Cerrar para aplicar
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error al restaurar: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    



}
