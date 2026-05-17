package secureauth.ui.components;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
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

import secureauth.ui.utils.JpanelR;
import secureauth.ui.utils.UiTheme;
import secureauth.ui.utils.factory.ButtonFactory;
import secureauth.ui.utils.factory.TableFactory;

/** Panel de reportes enterprise con estilo centralizado en UiTheme. */
public class PanelReports extends JPanel {

    private JLabel lblVentasDia;
    private JLabel lblVentasMes;
    private JLabel lblProductos;
    private JLabel lblClientes;
    private Runnable refreshAction;

    public PanelReports() {
        setLayout(new BorderLayout());
        setBackground(UiTheme.BG_PAGE);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(25, 25, 25, 25));

        content.add(buildHeader());
        content.add(Box.createVerticalStrut(25));
        content.add(buildKPIs());
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

    private JPanel buildHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Módulo de Reportes");
        title.setFont(UiTheme.TITLE_FONT_SECTION);
        title.setForeground(UiTheme.TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Dashboard analítico del negocio");
        subtitle.setFont(UiTheme.BODY_FONT);
        subtitle.setForeground(UiTheme.TEXT_SECONDARY);

        left.add(title);
        left.add(Box.createVerticalStrut(5));
        left.add(subtitle);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        JButton btnGenerate = ButtonFactory.primary("Actualizar", 150);
        JButton btnExport = ButtonFactory.dark("Exportar", 120);
        btnGenerate.addActionListener(e -> { if (refreshAction != null) refreshAction.run(); });

        actions.add(btnGenerate);
        actions.add(btnExport);

        panel.add(left, BorderLayout.WEST);
        panel.add(actions, BorderLayout.EAST);
        return panel;
    }

    private JPanel buildKPIs() {
        JPanel grid = new JPanel(new GridLayout(1, 4, 20, 0));
        grid.setOpaque(false);

        lblVentasDia = new JLabel("$0");
        lblVentasMes = new JLabel("$0");
        lblProductos = new JLabel("0");
        lblClientes = new JLabel("0");

        grid.add(createCard("Ventas del Día", lblVentasDia));
        grid.add(createCard("Ventas del Mes", lblVentasMes));
        grid.add(createCard("Productos Vendidos", lblProductos));
        grid.add(createCard("Clientes Nuevos", lblClientes));
        return grid;
    }

    private JPanel createCard(String title, JLabel value) {
        JpanelR card = new JpanelR();
        card.setArc(22);
        card.setBackgroundColor(UiTheme.PANEL_WHITE);
        card.setBorderConfig(UiTheme.BORDER_COLOR, 1f);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(20, 22, 20, 22));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(UiTheme.BODY_FONT.deriveFont(java.awt.Font.BOLD));
        lblTitle.setForeground(UiTheme.TEXT_SECONDARY);

        value.setFont(UiTheme.CARD_VALUE_FONT);
        value.setForeground(UiTheme.TEXT_PRIMARY);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildTableSection() {
        JpanelR card = new JpanelR();
        card.setArc(22);
        card.setBackgroundColor(UiTheme.PANEL_WHITE);
        card.setBorderConfig(UiTheme.BORDER_COLOR, 1f);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Resumen de Reportes");
        title.setFont(UiTheme.TITLE_FONT_SECTION);
        title.setForeground(UiTheme.TEXT_PRIMARY);

        String[] cols = {"ID", "Reporte", "Fecha", "Usuario", "Estado"};
        Object[][] data = {
                {"0001", "Ventas Mensuales", "08/05/2026", "admin", "Generado"},
                {"0002", "Inventario", "08/05/2026", "admin", "Pendiente"},
                {"0003", "Clientes", "08/05/2026", "admin", "Exportado"}
        };

        JTable table = new JTable(data, cols);
        TableFactory.applyEnterpriseStyle(table);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);

        card.add(title, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    public void updateMetrics(String ventasDia, String ventasMes, String productos, String clientes) {
        lblVentasDia.setText(ventasDia);
        lblVentasMes.setText(ventasMes);
        lblProductos.setText(productos);
        lblClientes.setText(clientes);
    }
}
