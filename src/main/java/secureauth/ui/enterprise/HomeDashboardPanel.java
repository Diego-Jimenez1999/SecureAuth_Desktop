package secureauth.ui.enterprise;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import secureauth.service.enterprise.SalesTransactionService;
import secureauth.ui.utils.UiTheme;

/** Panel Home enterprise con KPIs dinámicos del negocio/sucursal activa. */
public class HomeDashboardPanel extends JPanel {

    private final JLabel salesToday = metricLabel();
    private final JLabel salesMonth = metricLabel();
    private final JLabel gainMonth = metricLabel();
    private final JLabel itemsMonth = metricLabel();
    private final SalesTransactionService salesService = new SalesTransactionService();

    public HomeDashboardPanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(UiTheme.BG_PAGE);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel title = new JLabel("Home ERP - Resumen Ejecutivo");
        title.setFont(UiTheme.TITLE_FONT_SECTION);
        title.setForeground(UiTheme.TEXT_PRIMARY);

        JPanel grid = new JPanel(new GridLayout(2, 2, 10, 10));
        grid.setOpaque(false);
        grid.add(card("Ventas del Día", salesToday));
        grid.add(card("Ventas del Mes", salesMonth));
        grid.add(card("Ganancias del Mes", gainMonth));
        grid.add(card("Productos Vendidos", itemsMonth));

        add(title, BorderLayout.NORTH);
        add(grid, BorderLayout.CENTER);

        refresh();
    }

    public final void refresh() {
        try {
            salesService.initializeSchema();
            var s = salesService.loadStats();
            salesToday.setText(String.format("$ %,.0f", s.salesToday()));
            salesMonth.setText(String.format("$ %,.0f", s.salesMonth()));
            gainMonth.setText(String.format("$ %,.0f", s.gainMonth()));
            itemsMonth.setText(String.valueOf(s.itemsMonth()));
        } catch (Exception ignored) {
            salesToday.setText("$ 0");
            salesMonth.setText("$ 0");
            gainMonth.setText("$ 0");
            itemsMonth.setText("0");
        }
    }

    private JPanel card(String title, JLabel value) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UiTheme.PANEL_WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UiTheme.BORDER_COLOR), BorderFactory.createEmptyBorder(12, 12, 12, 12)));

        JLabel t = new JLabel(title);
        t.setFont(UiTheme.BODY_FONT.deriveFont(java.awt.Font.BOLD));
        t.setForeground(UiTheme.TEXT_SECONDARY);
        panel.add(t, BorderLayout.NORTH);
        panel.add(value, BorderLayout.CENTER);
        return panel;
    }

    private JLabel metricLabel() {
        JLabel label = new JLabel("0");
        label.setFont(UiTheme.CARD_VALUE_FONT);
        label.setForeground(UiTheme.TEXT_PRIMARY);
        return label;
    }
}
