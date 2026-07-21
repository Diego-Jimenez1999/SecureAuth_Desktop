package secureauth.ui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import secureauth.dao.enterprise.AppointmentDAO;
import secureauth.model.Appointment;
import secureauth.model.AppointmentStatus;
import secureauth.model.User;
import secureauth.service.OwnerService;
import secureauth.service.UserService;
import secureauth.service.enterprise.ActividadRecienteService;
import secureauth.service.enterprise.AppointmentService;
import secureauth.service.enterprise.InventoryService;
import secureauth.service.enterprise.SalesTransactionService;

/**
 * Panel Home del dashboard.
 *
 * <p>Muestra KPIs del día y del mes obtenidos en tiempo real desde la base de
 * datos. La carga de datos se realiza en un {@link SwingWorker} para no
 * bloquear la interfaz.</p>
 *
 * <ul>
 *   <li>Saludo dinámico con el nombre del usuario autenticado</li>
 *   <li>Ventas del día, ventas del mes, ítems vendidos en el mes</li>
 *   <li>Clientes nuevos este mes</li>
 * </ul>
 *
 * @author Diego
 * @version 2.1 — datos reales, SwingWorker, usuario dinámico.
 */
public final class HomeDashboardPanel extends JPanel {

    private static final Logger LOGGER = Logger.getLogger(HomeDashboardPanel.class.getName());
    private static final int ALERT_CARD_HEIGHT = 52;
    private static final int KPI_CARD_GAP = 16;
    private static final int KPI_CARD_PADDING = 14;
    private static final int KPI_INFO_TOP_GAP = 10;
    private static final int SUMMARY_PANEL_HEIGHT = 200;
    private static final int SUMMARY_SEPARATOR_HEIGHT = 140;
    private static final int SUMMARY_CARD_VERTICAL_PADDING = 6;
    private static final int SUMMARY_METRIC_ICON_WIDTH = 150;
    private static final int SUMMARY_METRIC_ICON_HEIGHT = 64;

    // KPI labels — actualizados desde el EDT tras carga en background
    private final JLabel salesTodayLabel   = new JLabel("Cargando...");
    private final JLabel salesMonthLabel   = new JLabel("Cargando...");
    private final JLabel itemsMonthLabel   = new JLabel("Cargando...");
    private final JLabel newClientsLabel   = new JLabel("Cargando...");
    private final JLabel newClientsSummaryLabel = new JLabel("Cargando...");
    private final JLabel welcomeLabel      = new JLabel("¡Bienvenido!");

    private final SalesTransactionService salesService;
    private final OwnerService ownerService;
    private final UserService userService;
    private final InventoryService inventoryService;
    private final ActividadRecienteService actividadService;
    private final AppointmentService appointmentService;
    private final NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.of("es", "CO"));
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM-dd HH:mm");
    private final DateTimeFormatter appointmentDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private DefaultTableModel activityModel;
    private DefaultTableModel movementsModel;
    private JTable appointmentsTable;
    private List<Appointment> appointmentRows = new ArrayList<>();
    private JLabel lowStockText;

    /**
     * Constructor sin usuario (compatibilidad con código existente en IngresoFrame).
     * El saludo usará un texto genérico hasta que se llame a {@link #setCurrentUser(User)}.
     */
    public HomeDashboardPanel() {
        this(null, new SalesTransactionService(), new OwnerService(new secureauth.dao.OwnerDAO()), new UserService(),
                new InventoryService(), new ActividadRecienteService(), new AppointmentService());
    }

    /**
     * Constructor con usuario autenticado para saludo personalizado.
     *
     * @param currentUser usuario que inició sesión
     */
    public HomeDashboardPanel(User currentUser) {
        this(currentUser, new SalesTransactionService(), new OwnerService(new secureauth.dao.OwnerDAO()),
                new UserService(), new InventoryService(), new ActividadRecienteService(), new AppointmentService());
    }

    /**
     * Constructor principal con servicios inyectados desde el bootstrap.
     *
     * @param currentUser usuario que inició sesión
     * @param salesService servicio de métricas de ventas
     * @param ownerService servicio de dueños/clientes
     * @param userService servicio de usuarios
     */
    public HomeDashboardPanel(User currentUser, SalesTransactionService salesService, OwnerService ownerService,
            UserService userService) {
        this(currentUser, salesService, ownerService, userService, new InventoryService(),
                new ActividadRecienteService(), new AppointmentService());
    }

    public HomeDashboardPanel(User currentUser, SalesTransactionService salesService, OwnerService ownerService,
            UserService userService, InventoryService inventoryService, ActividadRecienteService actividadService,
            AppointmentService appointmentService) {
        this.salesService = salesService;
        this.ownerService = ownerService;
        this.userService = userService;
        this.inventoryService = inventoryService;
        this.actividadService = actividadService;
        this.appointmentService = appointmentService;
        setCurrentUser(currentUser);
        build();
        AppointmentService.addAppointmentChangeListener(evt -> SwingUtilities.invokeLater(this::refresh));
    }

    /**
     * Actualiza el saludo con el nombre del usuario.
     *
     * @param user usuario autenticado
     */
    public void setCurrentUser(User user) {
        if (user != null) {
            welcomeLabel.setText("¡Bienvenido, " + user.getNombre() + " " + user.getApellido() + "!");
        }
    }

    /**
     * Recarga los KPIs desde la base de datos en hilo de fondo.
     * Llamar al navegar al Home para mantener datos actualizados.
     */
    public final void refresh() {
        // Indicar que está cargando
        salesTodayLabel.setText("...");
        salesMonthLabel.setText("...");
        itemsMonthLabel.setText("...");
        newClientsLabel.setText("...");
        newClientsSummaryLabel.setText("...");

        new SwingWorker<DashboardData, Void>() {
            @Override
            protected DashboardData doInBackground() throws Exception {
                salesService.initializeSchema();
                inventoryService.initializeSchema();
                actividadService.initializeSchema();
                appointmentService.initializeSchema();
                var stats = salesService.loadStats();
                int newClients = ownerService.countNewThisMonth();
                int newUsers = userService.countNewThisMonth();
                int scheduledAppointments = appointmentService.countScheduledAppointments();
                int finishedServices = appointmentService.countFinishedServices();
                List<Appointment> appointments = appointmentService.findDashboardAppointments(12);
                List<Object[]> movements = new ArrayList<>();
                
                for (var sale : salesService.recentSales(8)) {
                        movements.add(new Object[]{
                        emptyAs(sale.itemsSummary(), sale.itemsCount() + " item(s)"),
                        emptyAs(sale.paymentMethod(), "-"),
                        sale.itemsCount(),
                        currency.format(sale.total()),
                        sale.createdAt().format(dateFormatter)
                });
}
                List<Object[]> activities = new ArrayList<>();
                for (var activity : actividadService.recientes(10)) {
                    activities.add(new Object[]{
                            activity.fechaHora().format(DateTimeFormatter.ofPattern("hh:mm a")),
                            activity.descripcion(),
                            activity.tipo(),
                            emptyAs(activity.usuario(), "Sistema")
                    });
                }
                long lowStock = inventoryService.findAll("").stream().filter(i -> i.stock() <= i.minStock()).count();
                return new DashboardData(new long[]{
                        (long)(stats.salesToday()  * 100),
                        (long)(stats.salesMonth()  * 100),
                        scheduledAppointments,
                        newClients,
                        finishedServices,
                        newUsers
                }, movements, activities, appointments, lowStock);
            }

            @Override
            protected void done() {
                try {
                    DashboardData data = get();
                    long[] d = data.metrics();
                    salesTodayLabel.setText(currency.format(d[0] / 100.0));
                    salesMonthLabel.setText(currency.format(d[1] / 100.0));
                    itemsMonthLabel.setText(String.valueOf(d[2]));
                    newClientsLabel.setText(String.valueOf(d[3]));
                    newClientsSummaryLabel.setText(String.valueOf(d[4]));
                    renderMovements(data.movements());
                    renderAppointments(data.appointments());
                    if (lowStockText != null) {
                        lowStockText.setText(data.lowStockCount() == 0
                                ? "Inventario sin alertas de stock bajo"
                                : data.lowStockCount() + " producto(s) requieren reposición");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    setAllLabelsError();
                } catch (ExecutionException e) {
                    LOGGER.log(Level.WARNING, "Error cargando métricas del dashboard", e.getCause());
                    setAllLabelsError();
                }
            }
        }.execute();
    }

    // =========================================================
    // CONSTRUCCIÓN DE LA UI
    // =========================================================

    private void build() {
        setLayout(new BorderLayout(0, 16));
        setBackground(new Color(240, 242, 245));
        setBorder(new EmptyBorder(22, 28, 22, 28));

        // Saludo superior
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        welcomeLabel.setForeground(Color.BLACK);
        add(welcomeLabel, BorderLayout.NORTH);

        JPanel centerContent = new JPanel(new BorderLayout(0, 16));
        centerContent.setOpaque(false);

        JPanel topRow = new JPanel(new GridBagLayout());
        topRow.setOpaque(false);
        GridBagConstraints topGbc = new GridBagConstraints();
        topGbc.fill = GridBagConstraints.BOTH;
        topGbc.gridy = 0;
        topGbc.insets = new Insets(0, 0, 0, 16);
        topGbc.weighty = 0;

        topGbc.gridx = 0;
        topGbc.weightx = 0.72;
        topRow.add(createKpiPanel(), topGbc);

        topGbc.gridx = 1;
        topGbc.insets = new Insets(0, 0, 0, 0);
        topGbc.weightx = 0.28;
        topRow.add(createMesCard(), topGbc);

        JPanel tablesRow = new JPanel(new GridBagLayout());
        tablesRow.setOpaque(false);
        GridBagConstraints tableGbc = new GridBagConstraints();
        tableGbc.fill = GridBagConstraints.BOTH;
        tableGbc.gridy = 0;
        tableGbc.weightx = 0.5;
        tableGbc.weighty = 1;
        tableGbc.insets = new Insets(0, 0, 0, 16);

        JPanel leftColumn = new JPanel(new BorderLayout(0, 14));
        leftColumn.setOpaque(false);
        leftColumn.add(createActivityCard(), BorderLayout.CENTER);
        leftColumn.add(Box.createRigidArea(new Dimension(1, ALERT_CARD_HEIGHT)), BorderLayout.SOUTH);

        tableGbc.gridx = 0;
        tablesRow.add(leftColumn, tableGbc);

        JPanel rightColumn = new JPanel(new BorderLayout(0, 14));
        rightColumn.setOpaque(false);
        rightColumn.add(createMovementsCard(), BorderLayout.CENTER);
        rightColumn.add(createAlertCard(), BorderLayout.SOUTH);

        tableGbc.gridx = 1;
        tableGbc.insets = new Insets(0, 0, 0, 0);
        tablesRow.add(rightColumn, tableGbc);

        centerContent.add(topRow, BorderLayout.NORTH);
        centerContent.add(tablesRow, BorderLayout.CENTER);

        add(centerContent, BorderLayout.CENTER);
    }

    private JPanel createKpiPanel() {
        JPanel panel = card();
        panel.setLayout(new BorderLayout(0, 12));
        panel.setPreferredSize(new Dimension(720, SUMMARY_PANEL_HEIGHT));
        panel.setMinimumSize(new Dimension(520, SUMMARY_PANEL_HEIGHT));

        JLabel title = new JLabel("Resumen del Día");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(new Color(17, 24, 39));
        panel.add(title, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.X_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(4, 0, 0, 0));

        content.add(createSummaryImageLabel("/icon/H10101.png"));
        content.add(Box.createHorizontalStrut(KPI_CARD_GAP));
        content.add(createSummarySeparator());
        content.add(Box.createHorizontalStrut(KPI_CARD_GAP));
        content.add(createSummaryCard("/icon/H10102.png", "Citas Programadas", itemsMonthLabel));
        content.add(Box.createHorizontalStrut(KPI_CARD_GAP));
        content.add(createSummarySeparator());
        content.add(Box.createHorizontalStrut(KPI_CARD_GAP));
        content.add(createSummaryCard("/icon/H10103.png", "Servicios Finalizados", newClientsSummaryLabel));
        content.add(Box.createHorizontalStrut(KPI_CARD_GAP));
        content.add(createSummarySeparator());
        content.add(Box.createHorizontalStrut(KPI_CARD_GAP));
        content.add(createSummaryCard("/icon/H10104.png", "Ingresos del Día", salesTodayLabel));

        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    /*
        * Crea el panel de KPIs del mes con un diseño similar al de los KPIs del día pero con colores diferentes para diferenciarlos visualmente.
         * Incluye métricas clave como ventas del mes y clientes nuevos, cada una con su propio indicador de color para facilitar la identificación rápida.
         * El panel se integra perfectamente con el diseño general del dashboard, manteniendo la coherencia visual y la facilidad de lectura.
    */
    private JPanel createMesCard() {
        JPanel panel = card();
        panel.setLayout(new GridLayout(1, 2, KPI_CARD_GAP, 0));
        panel.setMinimumSize(new Dimension(240, 0));

        panel.add(createKpiCard("/icon/H10104.png", "Ventas del Mes", salesMonthLabel));
        panel.add(createKpiCard("/icon/H10102.png", "Clientes Nuevos/Mes", newClientsLabel));

        return panel;
    }

    /*
        * Crea la tarjeta de actividad reciente con un diseño limpio y moderno.
        * Muestra una lista de las actividades más recientes en el sistema.
        * El panel se adapta al tamaño disponible y mantiene una estética consistente con el resto del dashboard.
    */
    private JPanel createActivityCard() {
        JPanel panel = card();
        panel.setLayout(new BorderLayout(0, 10));

        JLabel lbl = new JLabel("Citas Agendadas");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 0));
        panel.add(lbl, BorderLayout.NORTH);

        activityModel = new DefaultTableModel(new String[]{
                "Mascota", "Tipo de Servicio", "Dueño", "Hora Citada", "Estado"
        }, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        appointmentsTable = new JTable(activityModel);
        applyModernTableStyle(appointmentsTable); // <- Aplicamos el estilo
        appointmentsTable.getColumnModel().getColumn(4).setCellRenderer(new AppointmentStatusRenderer());
        appointmentsTable.setComponentPopupMenu(createAppointmentsPopupMenu());
        appointmentsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                selectAppointmentRow(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                selectAppointmentRow(e);
            }
        });

        panel.add(createModernScrollPane(appointmentsTable), BorderLayout.CENTER); // <- Usamos el ScrollPane limpio
        return panel;
    }

    /**
     * Crea la tarjeta de movimientos recientes con un diseño limpio y moderno.
     * Muestra una lista de los últimos movimientos en el sistema.
     * El panel se adapta al tamaño disponible y mantiene una estética consistente con el resto del dashboard.
     */
    private JPanel createMovementsCard() {
        JPanel panel = card();
        panel.setLayout(new BorderLayout(0, 10));

        JLabel lbl = new JLabel("Últimos Movimientos de inventario");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 0));
        panel.add(lbl, BorderLayout.NORTH);

        movementsModel = new DefaultTableModel(new String[]{"Producto", "Metodo de Pago", "Cantidad", "Valor pago", "Fecha"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(movementsModel);
        applyModernTableStyle(table); // <- Aplicamos el estilo

        panel.add(createModernScrollPane(table), BorderLayout.CENTER); // <- Usamos el ScrollPane limpio
        return panel;
    }

    private JPanel createAlertCard() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        panel.setBackground(new Color(255, 245, 245));
        panel.setBorder(BorderFactory.createLineBorder(new Color(255, 200, 200)));
        panel.setPreferredSize(new Dimension(1, ALERT_CARD_HEIGHT));
        panel.setMinimumSize(new Dimension(1, ALERT_CARD_HEIGHT));

        JLabel icon = new JLabel("⚠");
        icon.setForeground(new Color(200, 60, 60));
        icon.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lowStockText = new JLabel("Revisa el inventario con stock bajo");
        lowStockText.setFont(new Font("Segoe UI", Font.BOLD, 13));

        panel.add(icon);
        panel.add(lowStockText);
        return panel;
    }

    private void renderMovements(List<Object[]> rows) {
        if (movementsModel == null) {
            return;
        }
        movementsModel.setRowCount(0);
        for (Object[] row : rows) {
            movementsModel.addRow(row);
        }
    }

    private void renderAppointments(List<Appointment> rows) {
        if (activityModel == null) {
            return;
        }
        appointmentRows = new ArrayList<>(rows);
        activityModel.setRowCount(0);
        for (Appointment appointment : rows) {
            activityModel.addRow(new Object[]{
                    appointment.getPetName(),
                    appointment.getServiceName(),
                    appointment.getOwnerName(),
                    appointment.getAppointmentDate().format(appointmentDateFormatter) + " "
                            + appointment.getAppointmentTime(),
                    appointment.getStatus()
            });
        }
        if (activityModel.getRowCount() == 0) {
            activityModel.addRow(new Object[]{"-", "Sin citas agendadas", "-", "-", "-"});
        }
    }

    private String emptyAs(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private JPanel card() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        return panel;
    }

    /**
     * Crea un JLabel con un icono centrado y un tamaño preferido para usarlo como separador visual en el panel de resumen.
     * @param imagePath
     * @return
     */
    private JLabel createSummaryImageLabel(String imagePath) {
        JLabel imageLabel = new JLabel(loadOriginalIcon(imagePath));
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setVerticalAlignment(SwingConstants.CENTER);
        imageLabel.setAlignmentY(Component.CENTER_ALIGNMENT);
        imageLabel.setPreferredSize(new Dimension(150, SUMMARY_SEPARATOR_HEIGHT));
        imageLabel.setMinimumSize(new Dimension(120, SUMMARY_SEPARATOR_HEIGHT));
        imageLabel.setMaximumSize(new Dimension(180, SUMMARY_SEPARATOR_HEIGHT));
        return imageLabel;
    }

    private JPanel createSummarySeparator() {
        JPanel separator = new JPanel(new BorderLayout());
        separator.setOpaque(false);
        separator.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(225, 229, 235)));
        separator.setPreferredSize(new Dimension(1, SUMMARY_SEPARATOR_HEIGHT));
        separator.setMinimumSize(new Dimension(1, SUMMARY_SEPARATOR_HEIGHT));
        separator.setMaximumSize(new Dimension(1, SUMMARY_SEPARATOR_HEIGHT));
        return separator;
    }

    private JPanel createSummaryCard(String imagePath, String title, JLabel valueLabel) {
        JPanel card = new JPanel();
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(SUMMARY_CARD_VERTICAL_PADDING, KPI_CARD_PADDING,
                SUMMARY_CARD_VERTICAL_PADDING, KPI_CARD_PADDING));
        card.setAlignmentY(Component.CENTER_ALIGNMENT);
        card.setMinimumSize(new Dimension(120, SUMMARY_SEPARATOR_HEIGHT));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, SUMMARY_SEPARATOR_HEIGHT));

        card.add(Box.createVerticalGlue());

        ImageIcon icon = loadOriginalIcon(imagePath);
        if (icon != null) {
            JLabel imageLabel = new JLabel(icon);
            imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
            imageLabel.setVerticalAlignment(SwingConstants.CENTER);
            imageLabel.setPreferredSize(new Dimension(SUMMARY_METRIC_ICON_WIDTH, SUMMARY_METRIC_ICON_HEIGHT));
            imageLabel.setMinimumSize(new Dimension(SUMMARY_METRIC_ICON_WIDTH, SUMMARY_METRIC_ICON_HEIGHT));
            imageLabel.setMaximumSize(new Dimension(SUMMARY_METRIC_ICON_WIDTH, SUMMARY_METRIC_ICON_HEIGHT));
            card.add(imageLabel);
        }

        card.add(Box.createVerticalStrut(KPI_INFO_TOP_GAP));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitle.setForeground(new Color(100, 116, 139));
        card.add(lblTitle);

        if (valueLabel != null) {
            card.add(Box.createVerticalStrut(8));
            valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
            valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
            valueLabel.setForeground(new Color(15, 23, 42));
            card.add(valueLabel);
        }

        card.add(Box.createVerticalGlue());
        return card;
    }

    private JPanel createKpiCard(String imagePath, String title, JLabel valueLabel) {
        JPanel card = new JPanel();
        card.setVisible(true); 
        card.setBackground(Color.WHITE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(235, 238, 242), 1),
                new EmptyBorder(KPI_CARD_PADDING, KPI_CARD_PADDING, KPI_CARD_PADDING, KPI_CARD_PADDING)
        ));

        ImageIcon icon = loadOriginalIcon(imagePath);
        if (icon != null) {
            JLabel imageLabel = new JLabel(icon);
            imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
            card.add(imageLabel);
        }

        if (title != null && !title.isBlank()) {
            JPanel info = new JPanel();
            info.setOpaque(false);
            info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
            info.setAlignmentX(Component.CENTER_ALIGNMENT);
            info.setBorder(new EmptyBorder(KPI_INFO_TOP_GAP, 0, 0, 0));

            JLabel lblTitle = new JLabel(title);
            lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
            lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
            lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblTitle.setForeground(new Color(100, 116, 139));
            info.add(lblTitle);

            if (valueLabel != null) {
                info.add(Box.createVerticalStrut(8));
                info.add(kpiSeparator());
                info.add(Box.createVerticalStrut(8));

                valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
                valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
                valueLabel.setForeground(new Color(15, 23, 42));
                info.add(valueLabel);

                info.add(Box.createVerticalStrut(8));
                info.add(kpiSeparator());
            }

            card.add(info);
        }

        return card;
    }

    /** Separador horizontal fino usado entre título/valor de cada tarjeta KPI (Cambio 3 y 4). */
    private JSeparator kpiSeparator() {
        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        separator.setForeground(new Color(235, 238, 242));
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        separator.setAlignmentX(Component.CENTER_ALIGNMENT);
        return separator;
    }

    /**
     * Carga un icono en su tamaño original. Cualquier error de carga se registra
     * y retorna {@code null} en vez de propagar la excepción, para no romper el
     * resto del panel.
     */
    private ImageIcon loadOriginalIcon(String imagePath) {
        try {
            java.net.URL resource = getClass().getResource(imagePath);
            if (resource == null) {
                LOGGER.log(Level.WARNING, "No se encontró el icono: {0}", imagePath);
                return null;
            }
            ImageIcon original = new ImageIcon(resource);
            int originalWidth = original.getIconWidth();
            int originalHeight = original.getIconHeight();
            if (originalWidth <= 0 || originalHeight <= 0) {
                LOGGER.log(Level.WARNING, "Icono inválido o vacío: {0}", imagePath);
                return null;
            }

            return original;
        } catch (RuntimeException ex) {
            LOGGER.log(Level.WARNING, "No se pudo cargar el icono: " + imagePath, ex);
            return null;
        }
    }


    private void setAllLabelsError() {
        salesTodayLabel.setText("--");
        salesMonthLabel.setText("--");
        itemsMonthLabel.setText("--");
        newClientsLabel.setText("--");
        newClientsSummaryLabel.setText("--");
    }

    private record DashboardData(long[] metrics, List<Object[]> movements, List<Object[]> activities,
                                List<Appointment> appointments,
                                long lowStockCount) { }

    private JPopupMenu createAppointmentsPopupMenu() {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem start = new JMenuItem("Iniciar Servicio");
        JMenuItem finish = new JMenuItem("Finalizar Servicio");
        JMenuItem cancel = new JMenuItem("Cancelar Cita");
        JMenuItem details = new JMenuItem("Ver Detalles");
        start.addActionListener(e -> updateSelectedAppointmentStatus(AppointmentDAO.STATUS_IN_PROGRESS));
        finish.addActionListener(e -> updateSelectedAppointmentStatus(AppointmentDAO.STATUS_DONE));
        cancel.addActionListener(e -> updateSelectedAppointmentStatus(AppointmentDAO.STATUS_CANCELLED));
        details.addActionListener(e -> showSelectedAppointmentDetails());
        menu.add(start);
        menu.add(finish);
        menu.add(cancel);
        menu.add(details);
        return menu;
    }

    private Appointment selectedAppointment() {
        if (appointmentsTable == null || appointmentsTable.getSelectedRow() < 0) {
            return null;
        }
        int modelRow = appointmentsTable.convertRowIndexToModel(appointmentsTable.getSelectedRow());
        if (modelRow < 0 || modelRow >= appointmentRows.size()) {
            return null;
        }
        return appointmentRows.get(modelRow);
    }

    private void selectAppointmentRow(MouseEvent event) {
        if (!event.isPopupTrigger() || appointmentsTable == null) {
            return;
        }
        int row = appointmentsTable.rowAtPoint(event.getPoint());
        if (row >= 0) {
            appointmentsTable.setRowSelectionInterval(row, row);
        }
    }

    private void updateSelectedAppointmentStatus(String status) {
        Appointment appointment = selectedAppointment();
        if (appointment == null || appointment.getId() == null) {
            JOptionPane.showMessageDialog(this, "Selecciona una cita válida.");
            return;
        }
        try {
            appointmentService.updateStatus(appointment.getId(), status);
            refresh();
        } catch (java.sql.SQLException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo actualizar la cita: " + ex.getMessage(),
                    "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showSelectedAppointmentDetails() {
        Appointment appointment = selectedAppointment();
        if (appointment == null) {
            JOptionPane.showMessageDialog(this, "Selecciona una cita válida.");
            return;
        }
        JOptionPane.showMessageDialog(this, createAppointmentDetailsPanel(appointment),
                "Detalle de Cita", JOptionPane.PLAIN_MESSAGE);
    }

    private JPanel createAppointmentDetailsPanel(Appointment appointment) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(14, 16, 14, 16));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 0, 5, 14);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridx = 0;
        gbc.gridy = 0;

        addDetailRow(panel, gbc, "Propietario", appointment.getOwnerName());
        addDetailRow(panel, gbc, "Mascota", appointment.getPetName());
        addDetailRow(panel, gbc, "Servicio", appointment.getServiceName());
        addDetailRow(panel, gbc, "Fecha", String.valueOf(appointment.getAppointmentDate()));
        addDetailRow(panel, gbc, "Hora", String.valueOf(appointment.getAppointmentTime()));
        addDetailRow(panel, gbc, "Estado", displayStatus(appointment.getStatus()));
        addDetailRow(panel, gbc, "Veterinario", emptyAs(appointment.getCreatedBy(), "-"));
        addDetailRow(panel, gbc, "Observaciones", emptyAs(appointment.getNotes(), "-"));

        return panel;
    }

    private void addDetailRow(JPanel panel, GridBagConstraints gbc, String label, String value) {
        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(new Font("Segoe UI", Font.BOLD, 13));
        labelComponent.setForeground(new Color(71, 85, 105));
        panel.add(labelComponent, gbc);

        JTextArea valueComponent = new JTextArea(value);
        valueComponent.setEditable(false);
        valueComponent.setOpaque(false);
        valueComponent.setLineWrap(true);
        valueComponent.setWrapStyleWord(true);
        valueComponent.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        valueComponent.setForeground(new Color(15, 23, 42));
        valueComponent.setBorder(null);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(valueComponent, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
    }

    private static String displayStatus(String status) {
        return AppointmentStatus.fromDatabaseValue(status)
                .map(AppointmentStatus::displayName)
                .orElse(status == null || status.isBlank() ? "-" : status);
    }


    /**
     * Aplica un diseño moderno y limpio a los JTable.
     */
    private void applyModernTableStyle(JTable table) {
        // 1. Estilo general de la tabla
        table.setRowHeight(38); // Más espacio entre filas
        table.setShowVerticalLines(false); // Fuera líneas robóticas
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(235, 238, 242)); // Línea separadora muy suave
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setForeground(new Color(50, 50, 50));
        table.setSelectionBackground(new Color(240, 245, 255)); // Azul muy claro al seleccionar
        table.setSelectionForeground(Color.BLACK);
        table.setFillsViewportHeight(true);
        table.setBackground(Color.WHITE);

        // 2. Estilo de la cabecera (Header)
        JTableHeader header = table.getTableHeader();
        header.setBackground(Color.WHITE);
        header.setForeground(new Color(120, 130, 140)); // Texto grisáceo
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setReorderingAllowed(false);

        // Renderizador personalizado para la cabecera (quitar bordes 3D y añadir padding)
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setBackground(Color.WHITE);
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 225, 230)), // Solo línea inferior
                        BorderFactory.createEmptyBorder(10, 15, 10, 15) // Padding interior
                ));
                return c;
            }
        });

        // 3. Renderizador personalizado para las celdas del cuerpo (añadir padding lateral)
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                // Quitar el borde de foco (focus) punteado por defecto y añadir padding
                ((JComponent) c).setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
                return c;
            }
        });
    }

    private static final class AppointmentStatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                    column);
            String status = value == null ? "" : value.toString();
            label.setText(displayStatus(status));
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(new Font("Segoe UI", Font.BOLD, 12));
            label.setOpaque(true);
            label.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
            if (AppointmentStatus.IN_PROGRESS.matches(status)) {
                label.setBackground(new Color(219, 234, 254));
                label.setForeground(new Color(30, 64, 175));
            } else if (AppointmentStatus.FINALIZED.matches(status)) {
                label.setBackground(new Color(220, 252, 231));
                label.setForeground(new Color(22, 101, 52));
            } else if (AppointmentStatus.CANCELLED.matches(status)) {
                label.setBackground(new Color(254, 226, 226));
                label.setForeground(new Color(153, 27, 27));
            } else if (AppointmentStatus.CONFIRMED.matches(status)) {
                label.setBackground(new Color(224, 242, 254));
                label.setForeground(new Color(3, 105, 161));
            } else {
                label.setBackground(new Color(254, 249, 195));
                label.setForeground(new Color(66, 32, 6));
            }
            return label;
        }
    }

    /**
     * Aplica estilo moderno al JScrollPane que envuelve a la tabla.
     */
    private JScrollPane createModernScrollPane(JTable table) {
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder()); // Quitar borde oscuro
        scrollPane.getViewport().setBackground(Color.WHITE); // Fondo blanco limpio
        return scrollPane;
    }

}
