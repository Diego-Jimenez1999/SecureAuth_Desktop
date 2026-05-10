package secureauth.ui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import secureauth.controller.IngresoController;
import secureauth.ui.components.table.ActionCellEditor;
import secureauth.ui.components.table.ActionCellRenderer;
import secureauth.ui.utils.JpanelR;

/**
 * Componente reutilizable para la sección de tabla y métricas del dashboard.
 *
 * @author Diego
 * @version 1.0
 */
public class UserTablePanel extends JPanel {

    private JTable table;

    /**
     * Constructor del panel de usuarios.
     *
     * @param parentFrame frame padre para diálogos
     * @param controller controlador del dashboard
     */
    public UserTablePanel(JFrame parentFrame, IngresoController controller) {
        setLayout(new BorderLayout(0, 15));
        setOpaque(false);

        add(buildMetricsPanel(), BorderLayout.NORTH);
        add(buildTableCard(parentFrame, controller), BorderLayout.CENTER);
    }

    private JPanel buildMetricsPanel() {
        JPanel metrics = new JPanel(new GridLayout(1, 4, 15, 0));
        metrics.setOpaque(false);
        metrics.setPreferredSize(new Dimension(0, 90));

        metrics.add(createMetricCard("Total Usuarios", "24", Color.WHITE, null));
        metrics.add(createMetricCard("Activos", "22", Color.WHITE, "92%"));
        metrics.add(createMetricCard("Suspendidos", "2", Color.WHITE, "8%"));
        metrics.add(createMetricCard("Hoy", "3", Color.WHITE, null));
        return metrics;
    }

    private JpanelR buildTableCard(JFrame parentFrame, IngresoController controller) {
        JpanelR tablePanel = new JpanelR();
        tablePanel.setBackgroundColor(Color.WHITE);
        tablePanel.setLayout(new BorderLayout());
        tablePanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        String[] columns = {"ID", "NOMBRE COMPLETO", "EMAIL", "GENERO", "ACCION"};
        DefaultTableModel model = new DefaultTableModel(columns, 5);

        table = new JTable(model);
        table.setRowHeight(45);
        table.setSelectionBackground(new Color(240, 240, 240));
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(230, 230, 230));

        table.getTableHeader().setBackground(Color.WHITE);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        table.getTableHeader().setForeground(Color.GRAY);

        table.getColumn("ACCION").setCellRenderer(new ActionCellRenderer());
        table.getColumn("ACCION").setCellEditor(new ActionCellEditor(new JCheckBox(), parentFrame, controller, table));

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);
        tablePanel.add(scroll, BorderLayout.CENTER);
        return tablePanel;
    }

    private JpanelR createMetricCard(String title, String value, Color bg, String percent) {
        JpanelR card = new JpanelR();
        card.setBackgroundColor(bg);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setForeground(Color.GRAY);
        lblTitle.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JLabel lblVal = new JLabel(value);
        lblVal.setFont(new Font("SansSerif", Font.BOLD, 22));

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblVal, BorderLayout.WEST);

        if (percent != null) {
            JLabel lblPerc = new JLabel(percent);
            lblPerc.setForeground(new Color(76, 175, 80));
            lblPerc.setFont(new Font("SansSerif", Font.BOLD, 12));
            card.add(lblPerc, BorderLayout.SOUTH);
        }

        return card;
    }

    /**
     * Retorna la tabla para acceso desde el controlador.
     *
     * @return tabla principal de usuarios
     */
    public JTable getTable() {
        return table;
    }
}
