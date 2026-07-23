package secureauth.ui.components;


import java.awt.BasicStroke;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import secureauth.config.DatabaseConnection;
import secureauth.service.ConfigurationService;
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
import javax.swing.ImageIcon;
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
import javax.swing.Scrollable;
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
import secureauth.ui.dialogs.AdvancedConfigDialog;
import secureauth.ui.dialogs.GestionVentasServiciosDialog;
import secureauth.ui.dialogs.PreciosPorTamanoDialog;
import secureauth.ui.dialogs.RegistroTrabajadores;
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

    private static final Logger LOGGER = Logger.getLogger(PanelConfig.class.getName());

    // ─── Componentes de métricas ──────────────────────────────────────────────
    private JLabel lblVentasMesValor;
    private JLabel lblVentasDiaValor;
    private JLabel lblClientesNuevosValor;
    private JLabel lblCitasPendientesValor;
    private JLabel lblStockBajoValor;

    private final secureauth.service.enterprise.SalesTransactionService salesTxService = new secureauth.service.enterprise.SalesTransactionService();
    private final secureauth.dao.enterprise.AppointmentDAO appointmentDAO = new secureauth.dao.enterprise.AppointmentDAO();
    private final secureauth.dao.OwnerDAO ownerDAO = new secureauth.dao.OwnerDAO();

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

        // Contenedor interior con padding.
        // FIX (resize/scroll bug): se usa ScrollableContentPanel en vez de un JPanel
        // plano. Un JPanel normal NO implementa Scrollable, por lo que JViewport
        // congela su ancho en el preferredSize calculado la primera vez y nunca lo
        // actualiza al tamaño real de la ventana. Esto hacía que los GridLayout(1,4)
        // de buildMetricsSection()/buildActionCardsSection() recibieran siempre el
        // mismo ancho "viejo", provocando que la última tarjeta quedara recortada
        // por el borde del JScrollPane al redimensionar. Ver ScrollableContentPanel
        // más abajo para el detalle de la corrección.
        JPanel content = new ScrollableContentPanel();
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

    private JPanel metricsRowPanel;

    /**
     * Construye la fila de cuatro tarjetas de métricas del dashboard superior.
     * Carga las preferencias de forma dinámica desde base de datos.
     */
    private JPanel buildMetricsSection() {
        metricsRowPanel = new JPanel();
        metricsRowPanel.setBackground(UiTheme.BG_PAGE);
        // FIX: se amplía el alto (110 -> 140) porque el título ahora puede
        // hacer salto de línea en vez de truncarse con "...". Sin este
        // espacio extra, un título de 2 líneas quedaría recortado igual.
        metricsRowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));

        rebuildMetricsRow();
        return metricsRowPanel;
    }

    public void rebuildMetricsRow() {
        if (metricsRowPanel == null) return;
        metricsRowPanel.removeAll();

        String visibleSetting = ConfigurationService.getInstance().getSetting(
            "visible_metrics_cards",
            "VENTAS_MES,SERVICIOS_POPULARES,INGRESOS_CATEGORIA,CLIENTES_NUEVOS"
        );
        String[] cards = visibleSetting.split(",");

        int visibleCount = Math.max(1, cards.length);
        metricsRowPanel.setLayout(new GridLayout(1, visibleCount, UiTheme.CARD_SPACING, 0));

        for (String card : cards) {
            card = card.trim().toUpperCase();
            switch (card) {
                case "VENTAS_MES" -> {
                    lblVentasMesValor = new JLabel("$0.00");
                    lblVentasMesValor.setFont(UiTheme.CARD_VALUE_FONT);
                    lblVentasMesValor.setForeground(UiTheme.TEXT_PRIMARY);
                    JLabel trend = trendLabel("↑ Crecimiento", UiTheme.SUCCESS_COLOR);
                    metricsRowPanel.add(buildMetricCard("💰", "Ventas Totales (Este Mes)", lblVentasMesValor, trend, UiTheme.ACCENT_BLUE));
                }
                case "VENTAS_DIA" -> {
                    lblVentasDiaValor = new JLabel("$0.00");
                    lblVentasDiaValor.setFont(UiTheme.CARD_VALUE_FONT);
                    lblVentasDiaValor.setForeground(UiTheme.TEXT_PRIMARY);
                    JLabel trend = trendLabel("Hoy", UiTheme.TEXT_SECONDARY);
                    metricsRowPanel.add(buildMetricCard("💵", "Ventas de Hoy", lblVentasDiaValor, trend, UiTheme.ACCENT_BLUE));
                }
                case "CLIENTES_NUEVOS" -> {
                    lblClientesNuevosValor = new JLabel("0");
                    lblClientesNuevosValor.setFont(UiTheme.CARD_VALUE_FONT);
                    lblClientesNuevosValor.setForeground(UiTheme.TEXT_PRIMARY);
                    JLabel trend = trendLabel("↑ Este Mes", UiTheme.SUCCESS_COLOR);
                    metricsRowPanel.add(buildMetricCard("👥", "Nuevos Clientes", lblClientesNuevosValor, trend, UiTheme.SUCCESS_COLOR));
                }
                case "CITAS_PENDIENTES" -> {
                    lblCitasPendientesValor = new JLabel("0");
                    lblCitasPendientesValor.setFont(UiTheme.CARD_VALUE_FONT);
                    lblCitasPendientesValor.setForeground(UiTheme.TEXT_PRIMARY);
                    JLabel trend = trendLabel("En Agenda", UiTheme.TEXT_SECONDARY);
                    metricsRowPanel.add(buildMetricCard("📅", "Citas Pendientes", lblCitasPendientesValor, trend, UiTheme.ACCENT_AMBER));
                }
                case "STOCK_BAJO" -> {
                    lblStockBajoValor = new JLabel("0");
                    lblStockBajoValor.setFont(UiTheme.CARD_VALUE_FONT);
                    lblStockBajoValor.setForeground(UiTheme.TEXT_PRIMARY);
                    JLabel trend = trendLabel("Alerta de Stock", UiTheme.ERROR_COLOR);
                    metricsRowPanel.add(buildMetricCard("⚠️", "Productos Stock Bajo", lblStockBajoValor, trend, UiTheme.ERROR_COLOR));
                }
                case "SERVICIOS_POPULARES" -> {
                    JPanel servContent = new JPanel(new GridLayout(2, 3, 4, 2));
                    servContent.setBackground(UiTheme.PANEL_WHITE);
                    String[] sLabels = {"Hotel", "Consulta", "Baño", "53%", "50%", "70%"};
                    for (int i = 0; i < sLabels.length; i++) {
                        JLabel l = new JLabel(sLabels[i], SwingConstants.CENTER);
                        l.setFont(i < 3 ? UiTheme.SMALL_FONT.deriveFont(Font.BOLD) : UiTheme.SMALL_FONT);
                        l.setForeground(i < 3 ? UiTheme.TEXT_PRIMARY : UiTheme.ACCENT_AMBER);
                        servContent.add(l);
                    }
                    metricsRowPanel.add(buildMetricCardCustom("⭐", "Servicios Más Populares", servContent, UiTheme.ACCENT_AMBER));
                }
                case "INGRESOS_CATEGORIA" -> {
                    JPanel ingContent = new JPanel(new GridLayout(2, 2, 8, 4));
                    ingContent.setBackground(UiTheme.PANEL_WHITE);
                    String[] cats = {"Alimentos", "53%", "Accesorios", "47%"};
                    Color[] catClr = {UiTheme.TEXT_PRIMARY, UiTheme.SUCCESS_COLOR, UiTheme.TEXT_PRIMARY, UiTheme.SUCCESS_COLOR};
                    for (int i = 0; i < cats.length; i++) {
                        JLabel l = new JLabel(cats[i]);
                        l.setFont(UiTheme.BODY_FONT);
                        l.setForeground(catClr[i]);
                        ingContent.add(l);
                    }
                    metricsRowPanel.add(buildMetricCardCustom("📊", "Ingresos por Categoría", ingContent, UiTheme.ACCENT_PURPLE));
                }
            }
        }

        metricsRowPanel.revalidate();
        metricsRowPanel.repaint();

        // Carga real de estadísticas en background hilos de forma asíncrona
        loadRealMetrics();
    }

    private void loadRealMetrics() {
        javax.swing.SwingWorker<Map<String, Object>, Void> worker = new javax.swing.SwingWorker<>() {
            @Override
            protected Map<String, Object> doInBackground() throws Exception {
                Map<String, Object> stats = new HashMap<>();
                int bizId = secureauth.service.enterprise.EnterpriseContext.getInstance().getActiveBusinessId();
                int branchId = secureauth.service.enterprise.EnterpriseContext.getInstance().getActiveBranchId();

                try {
                    secureauth.service.enterprise.SalesTransactionService.DashboardStats dStats = salesTxService.loadStats();
                    stats.put("sales_month", dStats.salesMonth());
                    stats.put("sales_today", dStats.salesToday());
                } catch (Exception e) {
                    stats.put("sales_month", 0.0);
                    stats.put("sales_today", 0.0);
                }

                try {
                    int newClientsVal = ownerDAO.countNewThisMonth();
                    stats.put("new_clients", newClientsVal);
                } catch (Exception e) {
                    stats.put("new_clients", 0);
                }

                try {
                    int pendingApptsVal = appointmentDAO.countScheduled();
                    stats.put("pending_appts", pendingApptsVal);
                } catch (Exception e) {
                    stats.put("pending_appts", 0);
                }

                try {
                    int lowStockVal = countLowStock(bizId, branchId);
                    stats.put("low_stock", lowStockVal);
                } catch (Exception e) {
                    stats.put("low_stock", 0);
                }

                return stats;
            }

            @Override
            protected void done() {
                try {
                    Map<String, Object> stats = get();
                    java.text.NumberFormat nf = java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("es", "CO"));

                    if (lblVentasMesValor != null) {
                        double val = (double) stats.getOrDefault("sales_month", 0.0);
                        lblVentasMesValor.setText(nf.format(val));
                    }
                    if (lblVentasDiaValor != null) {
                        double val = (double) stats.getOrDefault("sales_today", 0.0);
                        lblVentasDiaValor.setText(nf.format(val));
                    }
                    if (lblClientesNuevosValor != null) {
                        int val = (int) stats.getOrDefault("new_clients", 0);
                        lblClientesNuevosValor.setText(String.valueOf(val));
                    }
                    if (lblCitasPendientesValor != null) {
                        int val = (int) stats.getOrDefault("pending_appts", 0);
                        lblCitasPendientesValor.setText(String.valueOf(val));
                    }
                    if (lblStockBajoValor != null) {
                        int val = (int) stats.getOrDefault("low_stock", 0);
                        lblStockBajoValor.setText(String.valueOf(val));
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Error al actualizar métricas en UI", e);
                }
            }
        };
        worker.execute();
    }

    private int countLowStock(int bizId, int branchId) {
        String sql = "SELECT COUNT(*) FROM inventory_items WHERE business_id=? AND branch_id=? AND stock <= min_stock";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bizId);
            ps.setInt(2, branchId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Construye una tarjeta de métrica estándar con valor numérico.
     */
    private JPanel buildMetricCard(
                    String icon, String label,
                            JLabel valueLabel, JLabel trendLabel,Color accent) {

    RoundedPanel card = new RoundedPanel(
            UiTheme.BORDER_RADIUS,
            UiTheme.PANEL_WHITE);

    card.setLayout(new BorderLayout(0,12));
    card.setBorder(BorderFactory.createEmptyBorder(18,18,18,18));

    JPanel header = new JPanel(new BorderLayout(12,0));
    header.setOpaque(false);
    // FIX: se amplía el alto del header (72 -> 92) para dar espacio al
    // título cuando hace salto de línea en vez de truncarse.
    header.setPreferredSize(new Dimension(0,92));

    JPanel iconBadge = buildIconBadge(icon, accent);

    iconBadge.setPreferredSize(new Dimension(56,56));
    iconBadge.setMinimumSize(new Dimension(56,56));
    iconBadge.setMaximumSize(new Dimension(56,56));

    JPanel textPanel = new JPanel();
    textPanel.setOpaque(false);
    textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

    // FIX: se envuelve el texto en <html> para que, si el título no cabe
    // en una línea, haga salto de línea (wrap) en vez de recortarse con
    // "..." (JLabel solo trunca con "..." cuando el texto es un String
    // plano; con HTML, hace reflow del texto al ancho real disponible).
    JLabel title = new JLabel("<html>" + label + "</html>");
    title.setFont(UiTheme.SMALL_FONT.deriveFont(Font.BOLD));
    title.setForeground(UiTheme.TEXT_SECONDARY);

    textPanel.add(title);
    textPanel.add(Box.createVerticalStrut(6));
    textPanel.add(valueLabel);
    textPanel.add(Box.createVerticalStrut(4));
    textPanel.add(trendLabel);

    header.add(iconBadge, BorderLayout.WEST);
    header.add(textPanel, BorderLayout.CENTER);

    card.add(header, BorderLayout.CENTER);

    return card;
    }

    /**
     * Construye una tarjeta de métrica con contenido personalizado (sin valor único).
     * @param icon Carácter emoji para el ícono
     * @param label Título de la tarjeta
     * @param customContent Panel con contenido personalizado (ej. tabla, gráficos)
     * @param accent Color de acento para el ícono
     */
    private JPanel buildMetricCardCustom(
                        String icon,String label,
                                JPanel customContent,Color accent) {

    RoundedPanel card = new RoundedPanel(
            UiTheme.BORDER_RADIUS,
            UiTheme.PANEL_WHITE);

    card.setLayout(new BorderLayout(0, 12));
    card.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

    //-------------------------------------------------------
    // CABECERA
    //-------------------------------------------------------

    JPanel header = new JPanel(new BorderLayout(12, 0));
    header.setOpaque(false);

    // FIX: se amplía el alto del header (72 -> 92) para dar espacio al
    // título cuando hace salto de línea en vez de truncarse.
    header.setPreferredSize(new Dimension(0, 92));

    //-------------------------------------------------------
    // Icono
    //-------------------------------------------------------

    JPanel iconBadge = buildIconBadge(icon, accent);

    iconBadge.setPreferredSize(new Dimension(56,56));
    iconBadge.setMinimumSize(new Dimension(56,56));
    iconBadge.setMaximumSize(new Dimension(56,56));

    //-------------------------------------------------------
    // Texto
    //-------------------------------------------------------

    JPanel textPanel = new JPanel();
    textPanel.setOpaque(false);
    textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

    // FIX: <html> permite que el título haga salto de línea al ancho real
    // disponible en vez de truncarse con "..." (ver mismo fix en buildMetricCard).
    JLabel title = new JLabel("<html>" + label + "</html>");
    title.setFont(UiTheme.SMALL_FONT.deriveFont(Font.BOLD));
    title.setForeground(UiTheme.TEXT_SECONDARY);

    textPanel.add(title);
    textPanel.add(Box.createVerticalStrut(8));
    textPanel.add(customContent);

    //-------------------------------------------------------
    // Ensamblaje
    //-------------------------------------------------------

    header.add(iconBadge, BorderLayout.WEST);
    header.add(textPanel, BorderLayout.CENTER);

    card.add(header, BorderLayout.CENTER);

    return card;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  SECCIÓN 3 — TARJETAS DE ACCIÓN (4 MÓDULOS)
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Construye la fila de cuatros tarjetas de acción: Ventas, Inventario y Usuarios.
     *
     * @return JPanel con las 4 tarjetas de acción
     */
    private JPanel buildActionCardsSection() {
        JPanel row = new JPanel(new GridLayout(1, 4, UiTheme.CARD_SPACING, 0));
        row.setBackground(UiTheme.BG_PAGE);
        // FIX (crecer al maximizar): antes el alto máximo estaba fijo en
        // 230px, así que aunque la ventana se maximizara y sobrara espacio
        // vertical, las tarjetas de acción NUNCA crecían — ese espacio
        // quedaba vacío debajo del contenido dentro del JScrollPane. Ahora
        // el máximo es ilimitado: cuando ScrollableContentPanel detecta
        // espacio vertical disponible, BoxLayout reparte ese espacio extra
        // a esta fila, y GridLayout(1,4) lo divide por igual entre las 4
        // tarjetas, haciéndolas crecer proporcionalmente. El alto mínimo
        // (~190-230px, según el contenido de cada tarjeta) lo sigue
        // garantizando GridLayout a partir del preferredSize de las tarjetas.
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        // Tarjeta 1 — Gestión de Ventas y Servicios
        ImageIcon serviceIcon = new ImageIcon(//
        getClass().getResource("/icon/service.png"));

        row.add(buildActionCard(
            serviceIcon, "Gestión de Ventas y Servicios",
            "Configura categorías, subcategorías y precios dinámicos para guardería canina y otros tipos de negocio",
            new String[]{"Tabla de Servicios", "Precios por Tamaño"},
            new ActionListener[]{
                e -> onTablaServiciosClick(),
                e -> onPreciosTamanoClick()
            }
        ));

        // Tarjeta 2 — Control de Inventario y Productos
        ImageIcon inventoryIcon = new ImageIcon(//dirección de icono de inventario
        getClass().getResource("/icon/inventory.png"));
        row.add(buildActionCard(

            inventoryIcon, "Control de Inventario y Productos",
            "Administra productos físicos, stock mínimo y alertas, importación masiva",
            new String[]{"Ver Inventario", "Importar CSV/Excel"},
            new ActionListener[]{
                e -> onVerInventarioClick(),
                e -> onImportarCSVClick()
            }
        ));

        // Tarjeta 3 — Control de Usuarios (Trabajadores)
        ImageIcon usersIcon = new ImageIcon( //direccion de icono de usuarios
        getClass().getResource("/icon/users.png"));

        row.add(buildActionCard(
            usersIcon, "Control de Usuarios (Trabajadores)",
            "Administra cuentas de empleados, asigna roles (Admin, Vet, Recepcionista) y gestiona permisos",
            new String[]{"Lista de Trabajadores", "Nuevos Trabajadores"},
            new ActionListener[]{
                e -> onListaTrabajadoresClick(),
                e -> onNuevoTrabajadorClick()
            }
        ));

        // Tarjeta 4 — Configuración de la Aplicación
        ImageIcon configIcon = new ImageIcon( //direccion de icono de configuracion
        getClass().getResource("/icon/config_1.png"));

        row.add(buildActionCard(
            configIcon, "Configuración de la Aplicación",
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
    private JPanel buildActionCard(
                        ImageIcon icon, String title,
                                String description,String[] btnLabels,ActionListener[] listeners) {

    RoundedPanel card = new RoundedPanel(
            UiTheme.BORDER_RADIUS,
            UiTheme.PANEL_WHITE);

    card.setLayout(new BorderLayout(0, 12));
    card.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

    // FIX: ancho/alto mínimo para que el GridLayout(1,4) de
    // buildActionCardsSection() nunca comprima la tarjeta por debajo de lo
    // necesario para mostrar icono + texto + botones legibles.
    card.setMinimumSize(new Dimension(220, 210));

    //=========================================================
    // CABECERA
    //=========================================================

    JPanel header = new JPanel(new BorderLayout(12, 0));
    header.setOpaque(false);

    // FIX: se amplía el alto (80 -> 100) para dar espacio al título cuando
    // hace salto de línea en vez de truncarse con "...".
    header.setPreferredSize(new Dimension(0, 100));

    //---------------------------------------------------------
    // Imagen
    //---------------------------------------------------------

    JLabel image = new JLabel(icon);
    image.setHorizontalAlignment(SwingConstants.CENTER);
    image.setVerticalAlignment(SwingConstants.CENTER);

    // Todas las tarjetas utilizarán exactamente el mismo espacio.
    image.setPreferredSize(new Dimension(72, 72));
    image.setMinimumSize(new Dimension(72, 72));
    image.setMaximumSize(new Dimension(72, 72));

    //---------------------------------------------------------
    // Texto
    //---------------------------------------------------------

    JPanel textPanel = new JPanel();
    textPanel.setOpaque(false);
    textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

    // FIX: <html> permite que el título haga salto de línea al ancho real
    // disponible en vez de truncarse con "..." (antes: "Gestión de Ventas
    // y Ser...", "Control de Usuarios (Tr...", etc.)
    JLabel titleLbl = new JLabel("<html>" + title + "</html>");
    titleLbl.setFont(UiTheme.BODY_FONT.deriveFont(Font.BOLD, 13f));
    titleLbl.setForeground(UiTheme.TEXT_PRIMARY);

    // FIX: se elimina el ancho fijo "width:110px" del HTML. Ese ancho fijo
    // ignoraba el espacio real que la tarjeta recibía del GridLayout, y
    // cuando la columna era más angosta que 110px + icono + padding, el
    // texto quedaba recortado por el header. Ahora el <html> deja que el
    // JLabel haga wrap según el ancho real disponible en cada resize.
    JLabel descLbl = new JLabel("<html>" + description + "</html>");

    descLbl.setFont(UiTheme.BODY_FONT);
    descLbl.setForeground(UiTheme.TEXT_SECONDARY);

    textPanel.add(titleLbl);
    textPanel.add(Box.createVerticalStrut(6));
    textPanel.add(descLbl);

    header.add(image, BorderLayout.WEST);
    header.add(textPanel, BorderLayout.CENTER);

    //=========================================================
    // BOTONES
    //=========================================================

    // FIX: se reemplaza FlowLayout por GridLayout(1, n). FlowLayout envolvía
    // el segundo botón a una fila inferior cuando el ancho no alcanzaba, y
    // esa fila quedaba fuera del área reservada por BorderLayout.SOUTH
    // (calculada asumiendo una sola fila) → el botón "desaparecía" (se veía
    // solo "Tabla de Servicios" y no "Precios por Tamaño", por ejemplo).
    // Con GridLayout los botones siempre están en una sola fila, dividiendo
    // el ancho disponible entre ellos en vez de ocultarse.
    JPanel btnRow = new JPanel(new GridLayout(1, btnLabels.length, 8, 0));
    btnRow.setOpaque(false);

    for (int i = 0; i < btnLabels.length; i++) {
        JButton btn = buildDarkButton(btnLabels[i]);
        btn.addActionListener(listeners[i]);
        btnRow.add(btn);
    }

    //=========================================================
    // ENSAMBLAJE FINAL
    //=========================================================

    card.add(header, BorderLayout.CENTER);
    card.add(btnRow, BorderLayout.SOUTH);

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

        String[] methods = {"Efectivo", "Tarjeta", "Transferencia", "Nequi", "Daviplata"};
        String enabledSetting = ConfigurationService.getInstance().getSetting(
            "enabled_payment_methods",
            "Efectivo,Tarjeta,Transferencia,Nequi,Daviplata"
        );
        java.util.List<String> enabledList = java.util.Arrays.stream(enabledSetting.split(","))
                .map(String::trim)
                .map(String::toUpperCase)
                .toList();

        panel.add(title);
        panel.add(new JLabel()); // spacer

        final JCheckBox[] checkBoxes = new JCheckBox[methods.length];
        for (int i = 0; i < methods.length; i++) {
            String method = methods[i];
            boolean isChecked = enabledList.contains(method.toUpperCase());
            JCheckBox cb = new JCheckBox(method, isChecked);
            cb.setFont(UiTheme.BODY_FONT);
            cb.setBackground(UiTheme.PANEL_WHITE);
            cb.setForeground(UiTheme.TEXT_PRIMARY);
            checkBoxes[i] = cb;

            cb.addActionListener(e -> {
                java.util.List<String> activeList = new java.util.ArrayList<>();
                for (JCheckBox check : checkBoxes) {
                    if (check != null && check.isSelected()) {
                        activeList.add(check.getText());
                    }
                }
                String newVal = String.join(",", activeList);
                ConfigurationService.getInstance().setSetting("enabled_payment_methods", newVal, "Métodos de pago habilitados");
            });

            panel.add(cb);
        }
        return panel;
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
    private void onVerInventarioClick() {
        secureauth.ui.WindowManager.getInstance().showModule("INVENTARIO");
    }

    /** Abre el importador CSV/Excel.
     * @code InventoryController.importCSV()
     */
    private void onImportarCSVClick() {
        javax.swing.JFileChooser chooser = new javax.swing.JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Inventario CSV/XLSX", "csv", "xlsx"));
        Window window = SwingUtilities.getWindowAncestor(this);
        int result = chooser.showOpenDialog(window);
        if (result != javax.swing.JFileChooser.APPROVE_OPTION) return;

        java.io.File file = chooser.getSelectedFile();
        try {
            secureauth.service.enterprise.InventoryService invService = new secureauth.service.enterprise.InventoryService();
            secureauth.service.enterprise.InventoryService.ImportPreview preview = invService.previewImport(file);
            if (!preview.errors().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Errores detectados en la importación:\n" + String.join("\n", preview.errors()), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Se importarán " + preview.validRows().size() + " filas válidas de inventario.\n¿Deseas continuar?",
                    "Preview de importación", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                invService.importRows(preview.validRows());
                JOptionPane.showMessageDialog(this, "Inventario importado con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                // Reload metrics to reflect new products/stocks
                rebuildMetricsRow();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No se pudo importar el archivo: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

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
        new AdvancedConfigDialog(parent instanceof javax.swing.JFrame ? (javax.swing.JFrame) parent : null).setVisible(true);
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
     * Panel raíz del contenido dentro del {@link JScrollPane}.
     *
     * <p><b>Por qué existe esta clase (fix de redimensionamiento):</b> un
     * {@code JPanel} normal NO implementa {@link Scrollable}. Cuando eso pasa,
     * {@code JViewport}/{@code ViewportLayout} ignora el ancho real del
     * viewport y siempre le asigna a la vista su {@code preferredSize}
     * "congelado" (calculado la primera vez, a partir de la fila de tarjetas
     * más ancha). Como consecuencia, los {@code GridLayout(1,4,...)} de
     * {@code buildMetricsSection()} y {@code buildActionCardsSection()} nunca
     * recibían el ancho verdadero de la ventana al redimensionar, y la última
     * tarjeta terminaba recortada por el borde del scroll.</p>
     *
     * <p>Al implementar {@code Scrollable} y devolver {@code true} en
     * {@link #getScrollableTracksViewportWidth()}, forzamos a que el ancho de
     * este panel SIEMPRE sea igual al ancho real del viewport en cada resize,
     * mientras que el alto sigue calculándose por {@code preferredSize} (para
     * conservar el scroll vertical). Esto no cambia ningún layout manager
     * existente (BoxLayout, GridLayout, BorderLayout siguen intactos), solo
     * corrige el ancho que reciben.</p>
     */
    private static class ScrollableContentPanel extends JPanel implements Scrollable {

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(java.awt.Rectangle visibleRect, int orientation, int direction) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(java.awt.Rectangle visibleRect, int orientation, int direction) {
            return orientation == SwingConstants.VERTICAL ? visibleRect.height : visibleRect.width;
        }

        // CLAVE del fix: el ancho de este panel siempre sigue al del viewport,
        // así BoxLayout/GridLayout reciben el ancho real de la ventana.
        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        // El alto NO sigue al viewport de forma incondicional: solo lo hace
        // cuando sobra espacio vertical (ventana maximizada con poco
        // contenido). Así, las filas con maximumSize ilimitado en alto
        // (p. ej. buildActionCardsSection()) pueden crecer para llenar ese
        // espacio extra. Si el contenido es más alto que el viewport (caso
        // normal, muchos datos), se devuelve false y el scroll vertical
        // sigue funcionando exactamente igual que antes.
        @Override
        public boolean getScrollableTracksViewportHeight() {
            if (getParent() instanceof javax.swing.JViewport viewport) {
                return viewport.getHeight() > getPreferredSize().height;
            }
            return false;
        }
    }

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