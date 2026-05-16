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

import secureauth.dao.OwnerDAO;
import secureauth.dao.UserDAO;
import secureauth.model.User;
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

    private final SalesTransactionService salesService = new SalesTransactionService();
    private final OwnerDAO ownerDAO   = new OwnerDAO();
    private final UserDAO  userDAO    = new UserDAO();
    private final NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.of("es", "CO"));

    /**
     * Constructor sin usuario (compatibilidad con código existente en IngresoFrame).
     * El saludo usará un texto genérico hasta que se llame a {@link #setCurrentUser(User)}.
     */
    public HomeDashboardPanel() {
        build();
    }

    /**
     * Constructor con usuario autenticado para saludo personalizado.
     *
     * @param currentUser usuario que inició sesión
     */
    public HomeDashboardPanel(User currentUser) {
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

        new SwingWorker<long[], Void>() {
            @Override
            protected long[] doInBackground() throws Exception {
                salesService.initializeSchema();
                var stats = salesService.loadStats();
                int newClients = ownerDAO.countNewThisMonth();
                int newUsers   = userDAO.countNewThisMonth();
                // [salesToday * 100, salesMonth * 100, itemsMonth, newClients, newUsers]
                return new long[]{
                        (long)(stats.salesToday()  * 100),
                        (long)(stats.salesMonth()  * 100),
                        stats.itemsMonth(),
                        newClients,
                        newUsers
                };
            }

            @Override
            protected void done() {
                try {
                    long[] d = get();
                    salesTodayLabel.setText(currency.format(d[0] / 100.0));
                    salesMonthLabel.setText(currency.format(d[1] / 100.0));
                    itemsMonthLabel.setText(String.valueOf(d[2]));
                    newClientsLabel.setText(String.valueOf(d[3]));
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
        centerContent.add(createTableCard("Actividad Reciente",
                new String[]{"Módulo", "Detalle", "Hora"}), gbc);

        JPanel rightColumn = new JPanel(new BorderLayout(0, 20));
        rightColumn.setOpaque(false);
        rightColumn.add(createTableCard("Últimos Movimientos",
                new String[]{"Producto", "Acción", "Cant"}), BorderLayout.CENTER);
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

    private JPanel createTableCard(String title, String[] columns) {
        JPanel panel = card();
        panel.setLayout(new BorderLayout(0, 10));

        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        panel.add(lbl, BorderLayout.NORTH);

        DefaultTableModel mdl = new DefaultTableModel(columns, 5) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(mdl);
        table.setRowHeight(32);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(240, 240, 240));

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createAlertCard() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        panel.setBackground(new Color(255, 245, 245));
        panel.setBorder(BorderFactory.createLineBorder(new Color(255, 200, 200)));

        JLabel icon = new JLabel("⚠");
        icon.setForeground(new Color(200, 60, 60));
        icon.setFont(new Font("Segoe UI", Font.BOLD, 18));
        JLabel text = new JLabel("Revisa el inventario con stock bajo");
        text.setFont(new Font("Segoe UI", Font.BOLD, 13));

        panel.add(icon);
        panel.add(text);
        return panel;
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
}