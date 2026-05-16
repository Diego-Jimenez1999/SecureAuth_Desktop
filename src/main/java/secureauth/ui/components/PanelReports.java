package secureauth.ui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.JTableHeader;

import secureauth.ui.utils.JpanelR;

/**
 * Panel de reportes del sistema.
 * Incluye KPIs, gráficos y tablas resumen. Por ahora los gráficos son placeholders, pero se integrará JFreeChart para   visualizaciones reales.
 * @author Diego Gaviria Jimenez
 * @version 1.0.0
 */
public class PanelReports extends JPanel {

    private static final Color BG = new Color(245, 247, 250);
    private static final Color CARD = Color.WHITE;

    private static final Color TEXT = new Color(30, 41, 59);
    private static final Color SUBTEXT = new Color(100, 116, 139);

    private static final Color BLUE = new Color(37, 99, 235);
    private static final Color GREEN = new Color(22, 163, 74);
    private static final Color ORANGE = new Color(249, 115, 22);
    private static final Color RED = new Color(220, 38, 38);

    private JLabel lblVentasDia;
    private JLabel lblVentasMes;
    private JLabel lblProductos;
    private JLabel lblClientes;
    private Runnable refreshAction;

    public PanelReports() {

        setLayout(new BorderLayout());
        setBackground(BG);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(25, 25, 25, 25));

        content.add(buildHeader());
        content.add(Box.createVerticalStrut(25));
        content.add(buildKPIs());
        content.add(Box.createVerticalStrut(25));
        content.add(buildChartsSection());
        content.add(Box.createVerticalStrut(25));
        content.add(buildTableSection());

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        add(scroll, BorderLayout.CENTER);
    }

    public void setOnRefresh(Runnable action) {
        this.refreshAction = action;
    }

    // =========================
    // HEADER
    // =========================

    private JPanel buildHeader() {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Módulo de Reportes");
        title.setFont(new Font("Segoe UI", Font.BOLD, 30));
        title.setForeground(TEXT);

        JLabel subtitle = new JLabel("Dashboard analítico del negocio");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        subtitle.setForeground(SUBTEXT);

        left.add(title);
        left.add(Box.createVerticalStrut(5));
        left.add(subtitle);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        JButton btnGenerate = createPrimaryButton("Generar Reporte");
        JButton btnExport = createSecondaryButton("Exportar");
        btnGenerate.addActionListener(e -> {
            if (refreshAction != null) {
                refreshAction.run();
            }
        });

        actions.add(btnGenerate);
        actions.add(btnExport);

        panel.add(left, BorderLayout.WEST);
        panel.add(actions, BorderLayout.EAST);

        return panel;
    }

    // =========================
    // KPI CARDS
    // =========================

    private JPanel buildKPIs() {

        JPanel grid = new JPanel(new GridLayout(1, 4, 20, 0));
        grid.setOpaque(false);

        lblVentasDia = new JLabel("$0");
        lblVentasMes = new JLabel("$0");
        lblProductos = new JLabel("0");
        lblClientes = new JLabel("0");

        grid.add(createCard(
                "Ventas del Día",
                lblVentasDia,
                BLUE
        ));

        grid.add(createCard(
                "Ventas del Mes",
                lblVentasMes,
                GREEN
        ));

        grid.add(createCard(
                "Productos Vendidos",
                lblProductos,
                ORANGE
        ));

        grid.add(createCard(
                "Clientes Nuevos",
                lblClientes,
                RED
        ));

        return grid;
    }

    private JPanel createCard(String title, JLabel value, Color accent) {

        JpanelR card = new JpanelR();
        card.setArc(22);
        card.setBackgroundColor(CARD);
        card.setBorderConfig(new Color(230,230,230), 1f);

        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(20, 22, 20, 22));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(SUBTEXT);

        JPanel dot = new JPanel();
        dot.setBackground(accent);
        dot.setPreferredSize(new Dimension(14,14));

        top.add(lblTitle, BorderLayout.WEST);
        top.add(dot, BorderLayout.EAST);

        value.setFont(new Font("Segoe UI", Font.BOLD, 34));
        value.setForeground(TEXT);

        JLabel trend = new JLabel("↑ +12% este mes");
        trend.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        trend.setForeground(GREEN);

        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));

        bottom.add(value);
        bottom.add(Box.createVerticalStrut(10));
        bottom.add(trend);

        card.add(top, BorderLayout.NORTH);
        card.add(bottom, BorderLayout.CENTER);

        return card;
    }

    // =========================
    // CHARTS
    // =========================

    private JPanel buildChartsSection() {

        JPanel panel = new JPanel(new GridLayout(1, 2, 20, 0));
        panel.setOpaque(false);

        panel.add(createChartPlaceholder(
                "Tendencia de Ventas"
        ));

        panel.add(createChartPlaceholder(
                "Distribución de Productos"
        ));

        return panel;
    }

    private JPanel createChartPlaceholder(String title) {

        JpanelR card = new JpanelR();
        card.setArc(22);
        card.setBackgroundColor(CARD);
        card.setBorderConfig(new Color(230,230,230),1f);

        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(20,20,20,20));

        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lbl.setForeground(TEXT);

        JPanel fakeChart = new JPanel();
        fakeChart.setBackground(new Color(248,250,252));
        fakeChart.setBorder(BorderFactory.createDashedBorder(
                new Color(180,180,180)
        ));

        JLabel placeholder = new JLabel("Integrar JFreeChart aquí");
        placeholder.setForeground(SUBTEXT);

        fakeChart.add(placeholder);

        card.add(lbl, BorderLayout.NORTH);
        card.add(fakeChart, BorderLayout.CENTER);

        return card;
    }

    // =========================
    // TABLE SECTION
    // =========================

    private JPanel buildTableSection() {

        JpanelR card = new JpanelR();
        card.setArc(22);
        card.setBackgroundColor(CARD);
        card.setBorderConfig(new Color(230,230,230),1f);

        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(20,20,20,20));

        JLabel title = new JLabel("Resumen de Reportes");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(TEXT);

        String[] cols = {
                "ID",
                "Reporte",
                "Fecha",
                "Usuario",
                "Estado"
        };

        Object[][] data = {
                {"0001","Ventas Mensuales","08/05/2026","admin","Generado"},
                {"0002","Inventario","08/05/2026","admin","Pendiente"},
                {"0003","Clientes","08/05/2026","admin","Exportado"}
        };

        JTable table = new JTable(data, cols);

        table.setRowHeight(40);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(240,240,240));

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(new Color(248,250,252));

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);

        card.add(title, BorderLayout.NORTH);
        card.add(Box.createVerticalStrut(15), BorderLayout.CENTER);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(scroll);

        card.add(wrapper, BorderLayout.CENTER);

        return card;
    }

    // =========================
    // BUTTONS
    // =========================

    private JButton createPrimaryButton(String text) {

        JButton btn = new JButton(text);

        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(TEXT);

        btn.setFocusPainted(false);
        btn.setBorderPainted(false);

        btn.setPreferredSize(new Dimension(160, 42));

        return btn;
    }

    private JButton createSecondaryButton(String text) {

        JButton btn = new JButton(text);

        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(TEXT);
        btn.setBackground(Color.WHITE);

        btn.setFocusPainted(false);

        btn.setPreferredSize(new Dimension(120, 42));

        return btn;
    }

    // =========================
    // UPDATE METRICS
    // =========================

    public void updateMetrics(
            String ventasDia,
            String ventasMes,
            String productos,
            String clientes
    ) {

        lblVentasDia.setText(ventasDia);
        lblVentasMes.setText(ventasMes);
        lblProductos.setText(productos);
        lblClientes.setText(clientes);
    }
}