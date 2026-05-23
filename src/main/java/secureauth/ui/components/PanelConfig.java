package secureauth.ui.components;


import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

import secureauth.controller.AuthController;
import secureauth.controller.IngresoController;
import secureauth.dao.UserDAO;
import secureauth.repository.UserRepositoryImpl;
import secureauth.service.AuthService;
import secureauth.service.UserService;
import secureauth.ui.dialogs.GestionVentasServiciosDialog;
import secureauth.ui.dialogs.PreciosPorTamanoDialog;
import secureauth.ui.dialogs.RegistroTrabajadores;
import secureauth.ui.dialogs.ApplicationVisualConfigDialog;
import secureauth.ui.utils.UiTheme;

/**
 * Panel de Configuración y Ajustes del sistema SecureAuth Desktop (PetStore).
 *
 * <p>Actúa como el centro neurálgico del sistema. Centraliza la gestión de
 * parámetros globales, servicios, inventario y personal del negocio.</p>
 *
 * <p>Arquitectura MVC:
 * <ul>
 *   <li>Vista (este archivo): {@code SettingsPanel}</li>
 *   <li>Controlador (placeholder): {@code SettingsController} — pendiente de implementar</li>
 *   <li>Modelo (placeholder): {@code SettingsModel} — conectará a DB vía DAO</li>
 * </ul>
 * </p>
 *
 * <p>Dependencias externas recomendadas:
 * <ul>
 *   <li>FlatLaf (com.formdev:flatlaf) — configurar en la clase Main antes de crear ventanas</li>
 *   <li>MigLayout (com.miglayout:miglayout-swing) — layout engine profesional</li>
 * </ul>
 * </p>
 *
 * <pre>
 * // En Main.java o App.java, antes de new JFrame():
 * // FlatLightLaf.setup();
 * </pre>
 *
 * @author  Diego Jiménez (SecureAuth Desktop)
 * @version 2.0
 */
public class PanelConfig extends JPanel {

    // ─── Componentes de tabla (Dueños) ────────────────────────────────────────
    private JTable              workersTable;
    private DefaultTableModel   workersTableModel;
    private JTextField          searchField;
    private JPanel              peopleTablesContainer;
    private CardLayout          peopleTablesLayout;

    private final UserService   userService;
    private final IngresoController ingresoController;

    // ─── Componentes de métricas ──────────────────────────────────────────────
    private JLabel lblVentasValor;
    private JLabel lblClientesValor;

    // ─── Constructor ──────────────────────────────────────────────────────────

    /**
     * Construye e inicializa todos los componentes del panel de Ajustes.
     * Llama internamente a los métodos de construcción modular.
     * @param userService servicio de usuarios inyectado
     * @param ingresoController controlador principal para acciones de usuario
     */
    public PanelConfig(UserService userService, IngresoController ingresoController) {
        this.userService = userService;
        this.ingresoController = ingresoController;
        initComponents();
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  INICIALIZACIÓN PRINCIPAL
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Inicializa el layout raíz y ensambla las tres zonas verticales del panel.
     * Usa BorderLayout para el contenedor base y un JScrollPane para permitir scroll.
     */
    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(UiTheme.BG_PAGE);

        // Contenedor interior con padding
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(UiTheme.BG_PAGE);
        content.setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        // Ensamblaje de secciones verticales
        content.add(buildHeaderSection());
        content.add(Box.createVerticalStrut(UiTheme.DEFAULT_PADDING * 2));
        content.add(buildMetricsSection());
        content.add(Box.createVerticalStrut(UiTheme.CARD_SPACING));
        content.add(buildActionCardsSection());
        content.add(Box.createVerticalStrut(UiTheme.CARD_SPACING));
        content.add(buildPeopleTablesSection());
        content.add(Box.createVerticalStrut(UiTheme.CARD_SPACING));
        content.add(buildSystemConfigSection());
        content.add(Box.createVerticalStrut(16));

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(UiTheme.BG_PAGE);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    /**
     * Construye la sección de tablas de personas.
     * @return JPanel con la sección de tablas de personas
     */
    private JPanel buildPeopleTablesSection() {
        peopleTablesLayout = new CardLayout();
        peopleTablesContainer = new JPanel(peopleTablesLayout);
        peopleTablesContainer.setBackground(UiTheme.BG_PAGE);
        peopleTablesContainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 340));
        peopleTablesContainer.add(buildWorkersTableSection(), "workers");
        peopleTablesLayout.show(peopleTablesContainer, "workers"); // Mostrar la tabla de trabajadores por defecto
        return peopleTablesContainer;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  SECCIÓN 1 — CABECERA
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Construye la cabecera con título principal y subtítulo descriptivo.
     *
     * @return JPanel con la sección de encabezado
     */
    private JPanel buildHeaderSection() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UiTheme.BG_PAGE);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, UiTheme.HEADER_HEIGHT));

        JLabel title = new JLabel("Centro de Configuración y Ajustes");
        title.setFont(UiTheme.TITLE_FONT_SECTION);
        title.setForeground(UiTheme.TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Administra los parámetros globales del sistema");
        subtitle.setFont(UiTheme.SMALL_FONT);
        subtitle.setForeground(UiTheme.TEXT_SECONDARY);

        JPanel texts = new JPanel(new GridLayout(2, 1, 0, 2));
        texts.setBackground(UiTheme.BG_PAGE);
        texts.add(title);
        texts.add(subtitle);

        header.add(texts, BorderLayout.WEST);
        return header;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  SECCIÓN 2 — MÉTRICAS (4 TARJETAS)
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Construye la fila de cuatro tarjetas de métricas del dashboard superior.
     * Los valores son estáticos (placeholder); conectar al SettingsController para datos reales.
     *
     * @return JPanel con las 4 tarjetas de métricas
     */
    private JPanel buildMetricsSection() {
        JPanel row = new JPanel(new GridLayout(1, 4, UiTheme.CARD_SPACING, 0));
        row.setBackground(UiTheme.BG_PAGE);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        // Tarjeta 1 — Ventas Totales
        lblVentasValor = new JLabel("$2,503.64");
        lblVentasValor.setFont(UiTheme.CARD_VALUE_FONT);
        lblVentasValor.setForeground(UiTheme.TEXT_PRIMARY);
        JLabel ventasTrend = trendLabel("↑ 3 Crecimiento", UiTheme.SUCCESS_COLOR);
        row.add(buildMetricCard("💰", "Ventas Totales (Este Mes)", lblVentasValor,
                ventasTrend, UiTheme.ACCENT_BLUE));

        // Tarjeta 2 — Servicios Populares
        JPanel servContent = new JPanel(new GridLayout(2, 3, 4, 2));
        servContent.setBackground(UiTheme.PANEL_WHITE);
        String[] sLabels = {"Hotel", "Consulta", "Consulta", "53%", "50%", "70%"};
        for (int i = 0; i < sLabels.length; i++) {
            JLabel l = new JLabel(sLabels[i], SwingConstants.CENTER);
            l.setFont(i < 3 ? UiTheme.SMALL_FONT.deriveFont(Font.BOLD) : UiTheme.SMALL_FONT);
            l.setForeground(i < 3 ? UiTheme.TEXT_PRIMARY : UiTheme.ACCENT_AMBER);
            servContent.add(l);
        }
        // "Baño" en la primera posición de la segunda fila (ajuste visual)
        row.add(buildMetricCardCustom("⭐", "Servicios Más Populares", servContent, UiTheme.ACCENT_AMBER));

        // Tarjeta 3 — Ingresos por Categoría
        JPanel ingContent = new JPanel(new GridLayout(2, 2, 8, 4));
        ingContent.setBackground(UiTheme.PANEL_WHITE);
        String[] cats   = {"Alimentos", "$3%", "Accesorios", "$3%"};
        Color[]  catClr = {UiTheme.TEXT_PRIMARY, UiTheme.SUCCESS_COLOR, UiTheme.TEXT_PRIMARY, UiTheme.SUCCESS_COLOR};
        for (int i = 0; i < cats.length; i++) {
            JLabel l = new JLabel(cats[i]);
            l.setFont(UiTheme.BODY_FONT);
            l.setForeground(catClr[i]);
            ingContent.add(l);
        }
        row.add(buildMetricCardCustom("📊", "Ingresos por Categoría", ingContent, UiTheme.ACCENT_PURPLE));

        // Tarjeta 4 — Nuevos Clientes
        lblClientesValor = new JLabel("3");
        lblClientesValor.setFont(UiTheme.CARD_VALUE_FONT);
        lblClientesValor.setForeground(UiTheme.TEXT_PRIMARY);
        JLabel clientesTrend = trendLabel("↑ Crecimiento", UiTheme.SUCCESS_COLOR);
        row.add(buildMetricCard("👥", "Nuevos Clientes", lblClientesValor,
                clientesTrend, UiTheme.SUCCESS_COLOR));

        return row;
    }

    /**
     * Construye una tarjeta de métrica estándar con valor numérico.
     */
    private JPanel buildMetricCard(String icon, String label,
                                    JLabel valueLabel, JLabel trendLabel, Color accent) {
        RoundedPanel card = new RoundedPanel(UiTheme.BORDER_RADIUS, UiTheme.PANEL_WHITE);
        card.setLayout(new BorderLayout(8, 0));
        card.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // Ícono de acento
        JPanel iconBadge = buildIconBadge(icon, accent);

        // Textos
        JPanel texts = new JPanel(new GridLayout(3, 1, 0, 2));
        texts.setBackground(UiTheme.PANEL_WHITE);
        JLabel lbl = new JLabel(label);
        lbl.setFont(UiTheme.SMALL_FONT.deriveFont(Font.BOLD));
        lbl.setForeground(UiTheme.TEXT_SECONDARY);
        texts.add(lbl);
        texts.add(valueLabel);
        texts.add(trendLabel);

        card.add(iconBadge, BorderLayout.WEST);
        card.add(texts,     BorderLayout.CENTER);
        return card;
    }

    /**
     * Construye una tarjeta de métrica con contenido personalizado (sin valor único).
     */
    private JPanel buildMetricCardCustom(String icon, String label,
                                        JPanel customContent, Color accent) {
        RoundedPanel card = new RoundedPanel(UiTheme.BORDER_RADIUS, UiTheme.PANEL_WHITE);
        card.setLayout(new BorderLayout(8, 0));
        card.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel iconBadge = buildIconBadge(icon, accent);

        JPanel texts = new JPanel(new BorderLayout(0, 6));
        texts.setBackground(UiTheme.PANEL_WHITE);
        JLabel lbl = new JLabel(label);
        lbl.setFont(UiTheme.SMALL_FONT.deriveFont(Font.BOLD));
        lbl.setForeground(UiTheme.TEXT_SECONDARY);
        texts.add(lbl,           BorderLayout.NORTH);
        texts.add(customContent, BorderLayout.CENTER);

        card.add(iconBadge, BorderLayout.WEST);
        card.add(texts,     BorderLayout.CENTER);
        return card;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  SECCIÓN 3 — TARJETAS DE ACCIÓN (3 MÓDULOS)
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Construye la fila de tres tarjetas de acción: Ventas, Inventario y Usuarios.
     *
     * @return JPanel con las 3 tarjetas de acción
     */
    private JPanel buildActionCardsSection() {
        JPanel row = new JPanel(new GridLayout(1, 3, UiTheme.CARD_SPACING, 0));
        row.setBackground(UiTheme.BG_PAGE);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));

        // Tarjeta 1 — Gestión de Ventas y Servicios
        row.add(buildActionCard(
            "🛒", "Gestión de Ventas y Servicios",
            "Configura categorías, subcategorías y precios dinámicos para guardería canina y otros tipos de negocio",
            new String[]{"Tabla de Servicios", "Precios por Tamaño"},
            new ActionListener[]{
                e -> onTablaServiciosClick(),
                e -> onPreciosTamanoClick()
            }
        ));

        // Tarjeta 2 — Control de Inventario y Productos
        row.add(buildActionCard(
            "📦", "Control de Inventario y Productos",
            "Administra productos físicos, stock mínimo y alertas, importación masiva",
            new String[]{"Ver Inventario", "Importar CSV/Excel"},
            new ActionListener[]{
                e -> onVerInventarioClick(),
                e -> onImportarCSVClick()
            }
        ));

        // Tarjeta 3 — Control de Usuarios (Trabajadores)
        row.add(buildActionCard(
            "👷", "Control de Usuarios (Trabajadores)",
            "Administra cuentas de empleados, asigna roles (Admin, Vet, Recepcionista) y gestiona permisos",
            new String[]{"Lista de Trabajadores", "Nuevos Trabajadores"},
            new ActionListener[]{
                e -> onListaTrabajadoresClick(),
                e -> onNuevoTrabajadorClick()
            }
        ));

        row.add(configApp("🎨", "Configuración de la Aplicación",
                "Gestiona las opciones generales de configuración de la aplicación",
                new String[]{"Opciones Generales"},
                new ActionListener[]{e -> onConfigAppClick()}
        ));

        return row;
    }

    /**
     * Construye una tarjeta de acción individual con ícono, descripción y botones.
     *
     * @param icon         Carácter emoji para el ícono
     * @param title        Título de la tarjeta
     * @param description  Descripción breve del módulo
     * @param btnLabels    Etiquetas de los botones de acción
     * @param listeners    ActionListeners correspondientes a cada botón
     * @return JPanel estilizado como tarjeta de acción
     */
    private JPanel buildActionCard(String icon, String title, String description,
                                    String[] btnLabels, ActionListener[] listeners) {
        RoundedPanel card = new RoundedPanel(UiTheme.BORDER_RADIUS, UiTheme.PANEL_WHITE);
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Zona de íconos decorativos superiores
        JPanel iconRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        iconRow.setBackground(UiTheme.PANEL_WHITE);
        JLabel ico1 = new JLabel(icon);
        ico1.setPreferredSize(new Dimension(32,32));
        ico1.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 25));
        JLabel arrow = new JLabel("⇄");
        arrow.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        arrow.setForeground(UiTheme.TEXT_MUTED);
        JLabel ico2 = new JLabel("⚙️");
        ico2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 35));
        iconRow.add(ico1);
        iconRow.add(arrow);
        iconRow.add(ico2);

        // Título y descripción
        JPanel textBlock = new JPanel(new BorderLayout(0, 6));
        textBlock.setBackground(UiTheme.PANEL_WHITE);
        JLabel titleLbl = new JLabel("<html>" + title + "</html>");
        titleLbl.setFont(UiTheme.BODY_FONT.deriveFont(Font.BOLD));
        titleLbl.setForeground(UiTheme.TEXT_PRIMARY);
        JLabel descLbl = new JLabel("<html><p style='width:200px'>" + description + "</p></html>");
        descLbl.setFont(UiTheme.BODY_FONT);
        descLbl.setForeground(UiTheme.TEXT_SECONDARY);
        textBlock.add(titleLbl, BorderLayout.NORTH);
        textBlock.add(descLbl,  BorderLayout.CENTER);

        // Botones
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnRow.setBackground(UiTheme.PANEL_WHITE);
        for (int i = 0; i < btnLabels.length; i++) {
            JButton btn = buildDarkButton(btnLabels[i]);
            btn.addActionListener(listeners[i]);
            btnRow.add(btn);
        }

        card.add(iconRow,   BorderLayout.NORTH);
        card.add(textBlock, BorderLayout.CENTER);
        card.add(btnRow,    BorderLayout.SOUTH);
        return card;
    }

    /**
     * Construye el panel completo de la tabla de Trabajadores con buscador.
     * Este método reemplaza la sección de "Dueños" y se enfoca en la gestión de usuarios.
     *
     * @return JPanel con la tabla y barra de búsqueda para trabajadores
     */
    private JPanel buildWorkersTableSection() {
        RoundedPanel panel = new RoundedPanel(UiTheme.BORDER_RADIUS, UiTheme.PANEL_WHITE);
        panel.setLayout(new BorderLayout(0, 16));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 340));

        JPanel sectionHeader = new JPanel(new BorderLayout());
        sectionHeader.setBackground(UiTheme.PANEL_WHITE);
        JPanel sectionTitles = new JPanel(new GridLayout(2, 1, 0, 2));
        sectionTitles.setBackground(UiTheme.PANEL_WHITE);
        JLabel sTitle = new JLabel("Gestión de Personal (Trabajadores)"); // Título actualizado
        sTitle.setFont(UiTheme.BODY_FONT.deriveFont(Font.BOLD));
        sTitle.setForeground(UiTheme.TEXT_PRIMARY);
        JLabel sSub = new JLabel("Administra los usuarios y sus roles en el sistema"); // Subtítulo actualizado
        sSub.setFont(UiTheme.SMALL_FONT);
        sSub.setForeground(UiTheme.TEXT_SECONDARY);
        sectionTitles.add(sTitle);
        sectionTitles.add(sSub);

        JButton btnNuevoTrabajador = buildDarkButton("+ Registrar Nuevo Trabajador"); // Texto de botón actualizado
        btnNuevoTrabajador.addActionListener(e -> onNuevoTrabajadorClick());
        sectionHeader.add(sectionTitles, BorderLayout.WEST);
        sectionHeader.add(btnNuevoTrabajador, BorderLayout.EAST);

        // ── Tabla ──────────────────────────────────────────────────────────
        String[] columns = {"ID", "Nombre", "Apellido", "Email", "Rol", "Acción"}; // Columnas actualizadas
        workersTableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        workersTable = new JTable(workersTableModel);
        styleTable(workersTable);
        workersTable.getColumnModel().getColumn(5).setCellRenderer(new ActionButtonRenderer());
        workersTable.getColumnModel().getColumn(5).setCellEditor(
                new ActionButtonEditor(new JCheckBox(), workersTable, workersTableModel, userService, ingresoController)); // Pasa UserService y IngresoController

        // Anchos de columnas actualizados
        int[] widths = {40, 150, 150, 200, 120, 90};
        for (int i = 0; i < widths.length; i++) {
            workersTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        JScrollPane tableScroll = new JScrollPane(workersTable);
        tableScroll.setBorder(BorderFactory.createLineBorder(UiTheme.BORDER_COLOR));
        tableScroll.setPreferredSize(new Dimension(0, 160));

        // ── Barra de búsqueda rápida ───────────────────────────────────────
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        searchBar.setBackground(UiTheme.PANEL_WHITE);
        JLabel searchLabel = new JLabel("Consulta rápida");
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        searchLabel.setForeground(UiTheme.TEXT_PRIMARY);
        JLabel searchHint = new JLabel("Buscar trabajador por nombre o email"); // Placeholder actualizado
        searchHint.setFont(UiTheme.SMALL_FONT);
        searchHint.setForeground(UiTheme.TEXT_MUTED);
        searchBar.add(searchLabel);
        searchBar.add(searchHint);
        searchField = new JTextField(22); // Reutiliza el campo de búsqueda
        searchField.setFont(UiTheme.BODY_FONT);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiTheme.BORDER_COLOR),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        JButton btnConsultar = buildDarkButton("Consultar");
        btnConsultar.addActionListener(e -> onConsultarWorkersClick()); // Nuevo listener para búsqueda de trabajadores
        searchBar.add(searchField);
        searchBar.add(btnConsultar);

        panel.add(sectionHeader, BorderLayout.NORTH);
        panel.add(tableScroll, BorderLayout.CENTER);
        panel.add(searchBar, BorderLayout.SOUTH);

        return panel;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  SECCIÓN 5 — CONFIGURACIÓN DEL SISTEMA (TOGGLES)
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Construye el panel inferior de configuración del sistema con controles toggle.
     * Incluye: Notificaciones, Backups y Pasarelas de Pago.
     *
     * @return JPanel con los controles de configuración de sistema
     */
    private JPanel buildSystemConfigSection() {
        RoundedPanel panel = new RoundedPanel(UiTheme.BORDER_RADIUS, UiTheme.PANEL_WHITE);
        panel.setLayout(new BorderLayout(0, 16));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));

        JLabel sTitle = new JLabel("Configuración del Sistema");
        sTitle.setFont(UiTheme.BODY_FONT.deriveFont(Font.BOLD));
        sTitle.setForeground(UiTheme.TEXT_PRIMARY);

        JPanel toggleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 24, 8));
        toggleRow.setBackground(UiTheme.PANEL_WHITE);

        // Toggle — Notificaciones
        toggleRow.add(buildToggleItem("🔔 Notificaciones",
                "Alertas de citas y vencimientos", true));

        JSeparator sep1 = new JSeparator(SwingConstants.VERTICAL);
        sep1.setPreferredSize(new Dimension(1, 50));
        sep1.setForeground(UiTheme.BORDER_COLOR);
        toggleRow.add(sep1);

        // Toggle — Backup Automático
        toggleRow.add(buildToggleItem("🛡️ Seguridad / Backup",
                "Respaldo automático diario", false));

        JSeparator sep2 = new JSeparator(SwingConstants.VERTICAL);
        sep2.setPreferredSize(new Dimension(1, 50));
        sep2.setForeground(UiTheme.BORDER_COLOR);
        toggleRow.add(sep2);

        // Checkboxes — Métodos de Pago (tabla payment_methods)
        toggleRow.add(buildPaymentMethodsPanel());

        panel.add(sTitle,     BorderLayout.NORTH);
        panel.add(toggleRow,  BorderLayout.CENTER);
        return panel;
    }

    /**
     * Construye un ítem de toggle (JToggleButton estilizado) con etiqueta y descripción.
     *
     * @param label       Nombre del toggle
     * @param description Descripción breve
     * @param defaultOn   Estado inicial activado/desactivado
     * @return JPanel con el control toggle
     */
    private JPanel buildToggleItem(String label, String description, boolean defaultOn) {
        JPanel item = new JPanel(new BorderLayout(0, 4));
        item.setBackground(UiTheme.PANEL_WHITE);

        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        topRow.setBackground(UiTheme.PANEL_WHITE);

        JToggleButton toggle = new JToggleButton(defaultOn ? "ON" : "OFF");
        toggle.setSelected(defaultOn);
        toggle.setFont(new Font("Segoe UI", Font.BOLD, 10));
        toggle.setPreferredSize(new Dimension(56, 26));
        toggle.setBackground(defaultOn ? UiTheme.SUCCESS_COLOR : new Color(0xD1D5DB));
        toggle.setForeground(Color.WHITE);
        toggle.setBorderPainted(false);
        toggle.setFocusPainted(false);
        toggle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        toggle.addActionListener(e -> {
            boolean on = toggle.isSelected();
            toggle.setText(on ? "ON" : "OFF");
            toggle.setBackground(on ? UiTheme.SUCCESS_COLOR : new Color(0xD1D5DB));
            System.out.println("[DEBUG] Configuración: Notificaciones " + (on ? "ON" : "OFF"));
        });

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI Emoji", Font.BOLD, 13));
        lbl.setForeground(UiTheme.TEXT_PRIMARY);
        topRow.add(toggle);
        topRow.add(lbl);

        JLabel desc = new JLabel(description);
        desc.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 11));
        desc.setForeground(UiTheme.TEXT_SECONDARY);

        item.add(topRow, BorderLayout.NORTH);
        item.add(desc,   BorderLayout.CENTER);
        return item;
    }

    /**
     * Construye el panel de selección de métodos de pago activos.
     * Los checkboxes representan las filas de la tabla {@code payment_methods}.
     *
     * @return JPanel con los checkboxes de métodos de pago
     */
    private JPanel buildPaymentMethodsPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 8, 4));
        panel.setBackground(UiTheme.PANEL_WHITE);

        JLabel title = new JLabel("💳 Pasarelas de Pago Activas:");
        title.setFont(new Font("Segoe UI", Font.BOLD, 13));
        title.setForeground(UiTheme.TEXT_PRIMARY);

        // Placeholder — conectar a PaymentMethodDAO.getAll()
        String[] methods = {"Efectivo", "Tarjeta", "Nequi", "Daviplata"};
        panel.add(title);
        panel.add(new JLabel()); // spacer
        for (String method : methods) {
            JCheckBox cb = new JCheckBox(method, true);
            cb.setFont(UiTheme.BODY_FONT);
            cb.setBackground(UiTheme.PANEL_WHITE);
            cb.setForeground(UiTheme.TEXT_PRIMARY);
            cb.addActionListener(e -> 
                System.out.println("[Placeholder] Método " + method + " activo: " + cb.isSelected())
            );
            panel.add(cb);
        }
        return panel;
    }

    private JPanel configApp(String icon, String title, String description,
                                    String[] btnLabels, ActionListener[] listeners) {
        RoundedPanel card = new RoundedPanel(UiTheme.BORDER_RADIUS, UiTheme.PANEL_WHITE);
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Zona de íconos decorativos superiores
        JPanel iconRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        iconRow.setBackground(UiTheme.PANEL_WHITE);
        JLabel ico1 = new JLabel(icon);
        ico1.setPreferredSize(new Dimension(32,32));
        ico1.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 25));
        JLabel arrow = new JLabel("⇄");
        arrow.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        arrow.setForeground(UiTheme.TEXT_MUTED);
        JLabel ico2 = new JLabel("⚙️");
       // ico2.setPreferredSize(new Dimension(32,32));
        ico2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 30));
        iconRow.add(ico1);
        iconRow.add(arrow);
        iconRow.add(ico2);

        // Título y descripción
        JPanel textBlock = new JPanel(new BorderLayout(0, 6));
        textBlock.setBackground(UiTheme.PANEL_WHITE);
        JLabel titleLbl = new JLabel("<html>" + title + "</html>");
        titleLbl.setFont(UiTheme.BODY_FONT.deriveFont(Font.BOLD));
        titleLbl.setForeground(UiTheme.TEXT_PRIMARY);
        JLabel descLbl = new JLabel("<html><p style='width:200px'>" + description + "</p></html>");
        descLbl.setFont(UiTheme.BODY_FONT);
        descLbl.setForeground(UiTheme.TEXT_SECONDARY);
        textBlock.add(titleLbl, BorderLayout.NORTH);
        textBlock.add(descLbl,  BorderLayout.CENTER);

        // Botones
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnRow.setBackground(UiTheme.PANEL_WHITE);
        for (int i = 0; i < btnLabels.length; i++) {
            JButton btn = buildDarkButton(btnLabels[i]);
            btn.addActionListener(listeners[i]);
            btnRow.add(btn);
        }

        card.add(iconRow,   BorderLayout.NORTH);
        card.add(textBlock, BorderLayout.CENTER);
        card.add(btnRow,    BorderLayout.SOUTH);

        return card;
    }
    // ═════════════════════════════════════════════════════════════════════════
    //  HELPERS DE ESTILO
    // ═════════════════════════════════════════════════════════════════════════

    /** Aplica estilos visuales profesionales a un JTable. */
    private void styleTable(JTable table) {
        table.setFont(UiTheme.BODY_FONT);
        table.setRowHeight(38);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(UiTheme.BORDER_COLOR);
        table.setBackground(UiTheme.PANEL_WHITE);
        table.setSelectionBackground(new Color(0xEFF6FF));
        table.setSelectionForeground(UiTheme.TEXT_PRIMARY);
        table.setIntercellSpacing(new Dimension(0, 0));

        JTableHeader header = table.getTableHeader();
        header.setFont(UiTheme.SMALL_FONT.deriveFont(Font.BOLD));
        header.setBackground(new Color(0xF9FAFB));
        header.setForeground(UiTheme.TEXT_SECONDARY);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, UiTheme.BORDER_COLOR));
        header.setReorderingAllowed(false);
    }

    /** Crea un botón estilo oscuro (fondo #1F2937). */
    private JButton buildDarkButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(UiTheme.SMALL_FONT.deriveFont(Font.BOLD));
        btn.setBackground(UiTheme.BTN_DARK);
        btn.setForeground(Color.WHITE);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(UiTheme.BTN_DARK_HOVER); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(UiTheme.BTN_DARK); }
        });
        return btn;
    }
/** 
 * Crea un badge circular con ícono para las tarjetas de métricas.
 * Se asegura de que sea un círculo perfecto incluso si el panel se estira.
 */
private JPanel buildIconBadge(String icon, Color accent) {
    JPanel badge = new JPanel() {
        @Override 
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            // Suavizado de bordes para que el círculo no se vea pixelado
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Calculamos el tamaño del círculo (el lado más corto)
            int size = Math.min(getWidth(), getHeight());
            
            // Calculamos la posición para que siempre esté centrado
            int x = (getWidth() - size) / 2;
            int y = (getHeight() - size) / 2;
            
            // Color de fondo con transparencia (25 de alpha)
            g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 25));
            
            // Dibujamos el círculo usando el tamaño calculado
            g2.fillOval(x, y, size, size);
            
            g2.dispose();
            // Llamamos a super al final para que los componentes hijos (el emoji) se pinten encima
            super.paintComponent(g);
        }
    };

    badge.setOpaque(false);
    
    // Establecemos un tamaño fijo sugerido para ayudar al LayoutManager
    Dimension size = new Dimension(50, 50);
    badge.setPreferredSize(size);
    badge.setMinimumSize(size);
    badge.setMaximumSize(size);

    badge.setLayout(new GridBagLayout());
    
    JLabel iconLbl = new JLabel(icon);
    // Usamos Segoe UI Emoji para asegurar compatibilidad de iconos
    iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
    
    badge.add(iconLbl);
    return badge;
}

    /** Crea una etiqueta de tendencia con color. */
    private JLabel trendLabel(String text, Color color) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(color);
        return lbl;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  MÉTODOS PLACEHOLDER — CONECTAR AL CONTROLADOR
    // ═════════════════════════════════════════════════════════════════════════

    /** Abre el panel de tabla de servicios.
     * @code SettingsController.showServicesTable() */
    private void onTablaServiciosClick()    {
        Window window = SwingUtilities.getWindowAncestor(this);
        Frame parent = (window instanceof Frame) ? (Frame) window : null;
        new GestionVentasServiciosDialog(parent instanceof javax.swing.JFrame ? (javax.swing.JFrame) parent : null).setVisible(true);
    }

    /** Abre el configurador de precios por tamaño.
     * @code SettingsController.showPricesBySize()
     */
    private void onPreciosTamanoClick()     {
        Window window = SwingUtilities.getWindowAncestor(this);
        Frame parent = (window instanceof Frame) ? (Frame) window : null;
        new PreciosPorTamanoDialog(parent instanceof javax.swing.JFrame ? (javax.swing.JFrame) parent : null).setVisible(true);
    }

    /** Abre el visor de inventario.
     * @code InventoryController.show()
    */
    private void onVerInventarioClick()     { System.out.println("[DEBUG] Abriendo visor de inventario..."); }

    /** Abre el importador CSV/Excel.
     * @code InventoryController.importCSV()
     */
    private void onImportarCSVClick()       { System.out.println("[DEBUG] Abriendo importador CSV..."); }

    /** Abre la lista de trabajadores (tabla users) dentro de Configuración. */
    private void onListaTrabajadoresClick() {
        System.out.println("[DEBUG] PanelConfig: onListaTrabajadoresClick disparado.");
        loadWorkersTableData(null);
        if (peopleTablesLayout != null && peopleTablesContainer != null) {
            peopleTablesLayout.show(peopleTablesContainer, "workers");
            peopleTablesContainer.revalidate();
            peopleTablesContainer.repaint();
        }
    }

    /** Abre el panel de configuración general de la aplicación. */
    private void onConfigAppClick() {
        Window window = SwingUtilities.getWindowAncestor(this);
        Frame parent = (window instanceof Frame) ? (Frame) window : null;
        new ApplicationVisualConfigDialog(parent instanceof javax.swing.JFrame ? (javax.swing.JFrame) parent : null).setVisible(true);
    }

    /** Abre el formulario de registro de usuarios existente. */
    private void onNuevoTrabajadorClick() {
        System.out.println("[DEBUG] PanelConfig: onNuevoTrabajadorClick disparado.");
        
        // Obtener el Frame principal para el JDialog modal
        Window window = SwingUtilities.getWindowAncestor(this);
        Frame parentFrame = (window instanceof Frame) ? (Frame) window : null;

        // Instanciar el controlador con sus dependencias requeridas (Repository -> Service -> Controller)
        AuthController authController = new AuthController(new AuthService(new UserRepositoryImpl()));

        // Abrir el diálogo RegistroTrabajadores solicitado
        RegistroTrabajadores registrationDialog = new RegistroTrabajadores(parentFrame, authController);
        registrationDialog.setVisible(true);
        
        // Refrescar la tabla de trabajadores para mostrar el nuevo registro inmediatamente
        loadWorkersTableData(null);
    }

    /** Ejecuta la búsqueda de trabajadores. */
    private void onConsultarWorkersClick() {
        System.out.println("[DEBUG] PanelConfig: onConsultarWorkersClick disparado con query: " + searchField.getText());
        loadWorkersTableData(searchField.getText());
    }

    /**
     * Carga los datos de los trabajadores en la tabla.
     * @param query Cadena de búsqueda para filtrar trabajadores (puede ser nula o vacía para cargar todos).
     */
    private void loadWorkersTableData(String query) {
        if (workersTableModel == null) return;
        workersTableModel.setRowCount(0);

        // Fix: UserService actualmente solo soporta findAllWorkersWithRoleName() sin parámetros.
        // Aplicamos un filtro en memoria para mantener la funcionalidad de búsqueda.
        java.util.List<UserDAO.WorkerRow> workers = userService.findAllWorkersWithRoleName();
        
        if (query != null && !query.trim().isEmpty()) {
            String q = query.toLowerCase().trim();
            workers = workers.stream()
                .filter(w -> w.getNombre().toLowerCase().contains(q) || 
                            w.getApellido().toLowerCase().contains(q) || 
                            w.getEmail().toLowerCase().contains(q))
                .toList();
        }

        for (UserDAO.WorkerRow worker : workers) {
            workersTableModel.addRow(new Object[]{
                    worker.getId(),
                    worker.getNombre(),
                    worker.getApellido(),
                    worker.getEmail(),
                    worker.getRol(),
                    ""
            });
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  CLASES INTERNAS — COMPONENTES PERSONALIZADOS
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * JPanel con bordes redondeados y sombra suave.
     * Reemplaza el uso de JPanel + LineBorder para lograr el look de "card" moderno.
     */
    private static class RoundedPanel extends JPanel {
        private final int   radius;
        private final Color background;

        public RoundedPanel(int radius, Color background) {
            super();
            this.radius     = radius;
            this.background = background;
            setOpaque(false);
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Sombra
            g2.setColor(new Color(0, 0, 0, 12));
            g2.fill(new RoundRectangle2D.Double(2, 3, getWidth() - 4, getHeight() - 3, radius + 2, radius + 2));

            // Fondo
            g2.setColor(background);
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 2, radius, radius));

            // Borde sutil
            g2.setColor(UiTheme.BORDER_COLOR);
            g2.setStroke(new BasicStroke(1f));
            g2.draw(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 2, radius, radius));

            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * Renderizador de celda para la columna "Acción" de la tabla de Dueños.
     * Muestra un botón de editar (azul) y uno de eliminar (rojo).
     */
    private static class ActionButtonRenderer extends JPanel implements TableCellRenderer {
        private final JButton editBtn = new JButton("✏");
        private final JButton delBtn  = new JButton("🗑");

        public ActionButtonRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 4, 4));
            setBackground(UiTheme.PANEL_WHITE);
            styleActionBtn(editBtn, UiTheme.EDIT_BLUE);
            styleActionBtn(delBtn,  UiTheme.ERROR_COLOR);
            add(editBtn);
            add(delBtn);
        }

        private void styleActionBtn(JButton btn, Color color) {
            btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
            btn.setBackground(color);
            btn.setForeground(UiTheme.TEXT_LIGHT);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setPreferredSize(new Dimension(30, 26));
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v,
                boolean sel, boolean foc, int row, int col) {
            setBackground(sel ? new Color(0xEFF6FF) : UiTheme.PANEL_WHITE);
            return this;
        }
    }

    /**
     * Editor de celda para la columna "Acción" — maneja clicks en Editar y Eliminar Trabajadores.
     */
    private static class ActionButtonEditor extends DefaultCellEditor {
        private final JPanel          panel      = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 4));
        private final JButton         editBtn    = new JButton("✏");
        private final JButton         delBtn     = new JButton("🗑");
        private final UserService     userService;
        private final IngresoController ingresoController;

        public ActionButtonEditor(JCheckBox cb, JTable table, DefaultTableModel model, UserService userService, IngresoController ingresoController) {
            super(cb);
            this.userService = userService;
            this.ingresoController = ingresoController;
            panel.setBackground(UiTheme.PANEL_WHITE);
            styleBtn(editBtn, UiTheme.EDIT_BLUE);
            styleBtn(delBtn,  UiTheme.ERROR_COLOR);

            editBtn.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    int id = (int) model.getValueAt(row, 0);
                    System.out.println("[DEBUG] PanelConfig: Editando trabajador ID=" + id);
                this.ingresoController.editarUsuario(id); // Llama al método del controlador para editar
                    fireEditingStopped();
                }
            });

            delBtn.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    int id = (int) model.getValueAt(row, 0);
                    int confirm = JOptionPane.showConfirmDialog(panel,
                            "¿Eliminar trabajador ID " + id + "?", "Confirmar Eliminación",
                            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (confirm == JOptionPane.YES_OPTION) {
                    this.userService.delete(id); // Elimina el usuario a través del servicio
                        model.removeRow(row); // Elimina la fila de la tabla
                    }
                    fireEditingStopped();
                }
            });

            panel.add(editBtn);
            panel.add(delBtn);
        }

        private void styleBtn(JButton btn, Color color) {
            btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
            btn.setBackground(color);
            btn.setForeground(UiTheme.TEXT_LIGHT);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setPreferredSize(new Dimension(30, 26));
        }

        @Override public Component getTableCellEditorComponent(
                JTable t, Object v, boolean sel, int row, int col) { return panel; }
        @Override public Object getCellEditorValue() { return ""; }
    }

}
