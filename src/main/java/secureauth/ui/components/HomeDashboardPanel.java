package secureauth.ui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import secureauth.model.User;
import secureauth.service.OwnerService;
import secureauth.service.UserService;
import secureauth.service.enterprise.InventoryService;
import secureauth.service.enterprise.ActividadRecienteService;
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

    // KPI labels — actualizados desde el EDT tras carga en background
    private final JLabel salesTodayLabel   = new JLabel("Cargando...");
    private final JLabel salesMonthLabel   = new JLabel("Cargando...");
    private final JLabel itemsMonthLabel   = new JLabel("Cargando...");
    private final JLabel newClientsLabel   = new JLabel("Cargando...");
    private final JLabel welcomeLabel      = new JLabel("¡Bienvenido!");

    private final SalesTransactionService salesService;
    private final OwnerService ownerService;
    private final UserService userService;
    private final InventoryService inventoryService = new InventoryService();
    private final ActividadRecienteService actividadService = new ActividadRecienteService();
    private final NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.of("es", "CO"));
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM-dd HH:mm");
    private DefaultTableModel activityModel;
    private DefaultTableModel movementsModel;
    private JLabel lowStockText;

    /**
     * Constructor sin usuario (compatibilidad con código existente en IngresoFrame).
     * El saludo usará un texto genérico hasta que se llame a {@link #setCurrentUser(User)}.
     */
    public HomeDashboardPanel() {
        this(null, new SalesTransactionService(), new OwnerService(new secureauth.dao.OwnerDAO()), new UserService());
    }

    /**
     * Constructor con usuario autenticado para saludo personalizado.
     *
     * @param currentUser usuario que inició sesión
     */
    public HomeDashboardPanel(User currentUser) {
        this(currentUser, new SalesTransactionService(), new OwnerService(new secureauth.dao.OwnerDAO()), new UserService());
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
        this.salesService = salesService;
        this.ownerService = ownerService;
        this.userService = userService;
        setCurrentUser(currentUser);
        build();
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

        new SwingWorker<DashboardData, Void>() {
            @Override
            protected DashboardData doInBackground() throws Exception {
                salesService.initializeSchema();
                inventoryService.initializeSchema();
                actividadService.initializeSchema();
                var stats = salesService.loadStats();
                int newClients = ownerService.countNewThisMonth();
                int newUsers = userService.countNewThisMonth();
                List<Object[]> movements = new ArrayList<>();
                for (var sale : salesService.recentSales(8)) {
                    movements.add(new Object[]{
                            emptyAs(sale.itemsSummary(), sale.itemsCount() + " item(s)"),
                            sale.itemsCount(),
                            currency.format(sale.total()),
                            sale.createdAt().format(dateFormatter),
                            emptyAs(sale.userName(), "Sistema")
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
                        stats.itemsMonth(),
                        newClients,
                        newUsers
                }, movements, activities, lowStock);
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
                    renderMovements(data.movements());
                    renderActivities(data.activities());
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
        setLayout(new BorderLayout(0, 20));
        setBackground(new Color(240, 242, 245));
        setBorder(new EmptyBorder(25, 30, 25, 30));

        // Saludo superior
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        welcomeLabel.setForeground(Color.BLACK);
        add(welcomeLabel, BorderLayout.NORTH);

        // Contenedor central
        JPanel centerContent = new JPanel(new GridBagLayout());
        centerContent.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(10, 10, 10, 10);

        // Fila 1: KPIs del día
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.weightx = 0.7; gbc.weighty = 0.3;
        centerContent.add(createKpiPanel(), gbc);

        gbc.gridx = 2; gbc.gridy = 0; gbc.gridwidth = 1; gbc.weightx = 0.3;
        centerContent.add(createMesCard(), gbc);

        // Fila 2: Tablas de actividad y alertas
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2; gbc.weightx = 0.6; gbc.weighty = 0.7;
        centerContent.add(createActivityCard(), gbc);

        JPanel rightColumn = new JPanel(new BorderLayout(0, 20));
        rightColumn.setOpaque(false);
        rightColumn.add(createMovementsCard(), BorderLayout.CENTER);
        rightColumn.add(createAlertCard(), BorderLayout.SOUTH);

        gbc.gridx = 2; gbc.gridy = 1; gbc.gridwidth = 1; gbc.weightx = 0.4;
        centerContent.add(rightColumn, gbc);

        add(centerContent, BorderLayout.CENTER);
    }

    private JPanel createKpiPanel() {
        JPanel panel = card();
        panel.setLayout(new BorderLayout());

        JLabel title = new JLabel("Resumen del Día");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        panel.add(title, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(1, 2, 20, 0));
        grid.setOpaque(false);
        grid.setBorder(new EmptyBorder(15, 0, 0, 0));

        grid.add(kpiItem("Ventas del Día",  salesTodayLabel, new Color(255, 165, 0)));
        grid.add(kpiItem("Ítems del Mes",   itemsMonthLabel, new Color(72, 209, 204)));

        panel.add(grid, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createMesCard() {
        JPanel panel = card();
        panel.setLayout(new GridLayout(2, 1, 0, 12));

        panel.add(kpiItem("Ventas del Mes",      salesMonthLabel, new Color(100, 149, 237)));
        panel.add(kpiItem("Clientes Nuevos/Mes", newClientsLabel, new Color(120, 200, 100)));

        return panel;
    }

    private JPanel kpiItem(String title, JLabel valueLabel, Color color) {
        JPanel item = new JPanel(new BorderLayout(10, 5));
        item.setOpaque(false);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setForeground(Color.GRAY);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));

        JLabel dot = new JLabel("●", SwingConstants.CENTER);
        dot.setForeground(color);
        dot.setFont(new Font("Segoe UI", Font.PLAIN, 20));

        item.add(lblTitle,    BorderLayout.NORTH);
        item.add(valueLabel,  BorderLayout.CENTER);
        item.add(dot,         BorderLayout.EAST);
        return item;
    }

    private JPanel createActivityCard() {
        JPanel panel = card();
        panel.setLayout(new BorderLayout(0, 10));
        JLabel lbl = new JLabel("Actividad Reciente");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        panel.add(lbl, BorderLayout.NORTH);
        activityModel = new DefaultTableModel(new String[]{"Fecha", "Actividad", "Tipo", "Usuario"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(activityModel);
        table.setRowHeight(30);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createMovementsCard() {
        JPanel panel = card();
        panel.setLayout(new BorderLayout(0, 10));
        JLabel lbl = new JLabel("Últimos Movimientos");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        panel.add(lbl, BorderLayout.NORTH);
        movementsModel = new DefaultTableModel(new String[]{"Producto", "Cantidad", "Valor pago", "Fecha", "Usuario"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(movementsModel);
        table.setRowHeight(30);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createAlertCard() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        panel.setBackground(new Color(255, 245, 245));
        panel.setBorder(BorderFactory.createLineBorder(new Color(255, 200, 200)));

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

    private void renderActivities(List<Object[]> rows) {
        if (activityModel == null) {
            return;
        }
        activityModel.setRowCount(0);
        for (Object[] row : rows) {
            activityModel.addRow(row);
        }
        if (activityModel.getRowCount() == 0) {
            activityModel.addRow(new Object[]{"-", "Sin actividad reciente", "-", "-"});
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
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        return panel;
    }

    private void setAllLabelsError() {
        salesTodayLabel.setText("--");
        salesMonthLabel.setText("--");
        itemsMonthLabel.setText("--");
        newClientsLabel.setText("--");
    }

    private record DashboardData(long[] metrics, List<Object[]> movements, List<Object[]> activities,
                                 long lowStockCount) { }
}
