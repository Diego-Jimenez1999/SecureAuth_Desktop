package secureauth.ui.components;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import secureauth.model.enterprise.InventoryItem;
import secureauth.ui.utils.UiTheme;
import secureauth.ui.utils.factory.ButtonFactory;
import secureauth.ui.utils.factory.TableFactory;

/** Panel de inventario enterprise con estilos centralizados. */
public class PanelInventory extends JPanel {

    private final DefaultTableModel tableModel;
    private final JTable inventoryTable;

    private final JTextField txtSearch;
    private final JButton btnConsultar;
    private final JButton btnExport;

    private final JLabel lblTotalProducts;
    private final JLabel lblLowStock;

    public PanelInventory() {
        setLayout(new BorderLayout(20, 20));
        setBackground(UiTheme.BG_PAGE);
        setBorder(new EmptyBorder(25, 25, 25, 25));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Módulo de Inventario");
        lblTitle.setFont(UiTheme.TITLE_FONT_SECTION);
        lblTitle.setForeground(UiTheme.TEXT_PRIMARY);

        JLabel lblSubtitle = new JLabel("Dashboard de control y gestión de productos");
        lblSubtitle.setFont(UiTheme.BODY_FONT);
        lblSubtitle.setForeground(UiTheme.TEXT_SECONDARY);

        titlePanel.add(lblTitle);
        titlePanel.add(Box.createVerticalStrut(4));
        titlePanel.add(lblSubtitle);

        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actionsPanel.setOpaque(false);

        txtSearch = new JTextField();
        txtSearch.setPreferredSize(new Dimension(220, 34));
        txtSearch.setFont(UiTheme.BODY_FONT);
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiTheme.BORDER_COLOR),
                new EmptyBorder(0, 12, 0, 12)
        ));

        btnConsultar = ButtonFactory.dark("Ver Inventario", 160);
        btnExport = ButtonFactory.primary("Importar CSV/Excel", 180);

        actionsPanel.add(txtSearch);
        actionsPanel.add(btnConsultar);
        actionsPanel.add(btnExport);

        header.add(titlePanel, BorderLayout.WEST);
        header.add(actionsPanel, BorderLayout.EAST);

        JPanel metricsPanel = new JPanel(new java.awt.GridLayout(1, 2, 18, 0));
        metricsPanel.setOpaque(false);

        JPanel card1 = createMetricCard("Total Productos", "0", "Productos registrados");
        JPanel card2 = createMetricCard("Stock Bajo", "0", "Requieren reposición");
        lblTotalProducts = getMetricValue(card1);
        lblLowStock = getMetricValue(card2);
        metricsPanel.add(card1);
        metricsPanel.add(card2);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(UiTheme.PANEL_WHITE);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiTheme.BORDER_COLOR),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel lblTable = new JLabel("Resumen de Inventario");
        lblTable.setFont(UiTheme.BODY_FONT.deriveFont(java.awt.Font.BOLD, 18f));
        lblTable.setBorder(new EmptyBorder(0, 0, 15, 0));

        String[] columns = {"SKU", "Producto", "Categoría", "Stock", "Mínimo", "Estado"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        inventoryTable = new JTable(tableModel);
        TableFactory.applyEnterpriseStyle(inventoryTable);

        JScrollPane scrollPane = new JScrollPane(inventoryTable);
        scrollPane.setBorder(null);

        tablePanel.add(lblTable, BorderLayout.NORTH);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        JPanel center = new JPanel(new BorderLayout(0, 20));
        center.setOpaque(false);
        center.add(metricsPanel, BorderLayout.NORTH);
        center.add(tablePanel, BorderLayout.CENTER);

        add(header, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
    }

    private JPanel createMetricCard(String title, String value, String subtitle) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(UiTheme.PANEL_WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiTheme.BORDER_COLOR),
                new EmptyBorder(18, 18, 18, 18)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(UiTheme.SMALL_FONT);
        lblTitle.setForeground(UiTheme.TEXT_SECONDARY);

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(UiTheme.CARD_VALUE_FONT);
        lblValue.setForeground(UiTheme.TEXT_PRIMARY);

        JLabel lblSub = new JLabel(subtitle);
        lblSub.setFont(UiTheme.SMALL_FONT);
        lblSub.setForeground(UiTheme.TEXT_SECONDARY);

        card.add(lblTitle);
        card.add(Box.createVerticalStrut(10));
        card.add(lblValue);
        card.add(Box.createVerticalStrut(6));
        card.add(lblSub);
        return card;
    }

    private JLabel getMetricValue(JPanel panel) {
        return (JLabel) panel.getComponent(2);
    }

    public void renderItems(List<InventoryItem> items) {
        tableModel.setRowCount(0);
        int lowStock = 0;

        for (InventoryItem item : items) {
            String status;
            if (item.stock() <= item.minStock()) {
                status = "Stock Bajo";
                lowStock++;
            } else {
                status = "Disponible";
            }

            tableModel.addRow(new Object[]{item.sku(), item.name(), item.category(), item.stock(), item.minStock(), status});
        }

        lblTotalProducts.setText(String.valueOf(items.size()));
        lblLowStock.setText(String.valueOf(lowStock));
    }

    public void setSearchAction(ActionListener listener) {
        txtSearch.addActionListener(listener);
        btnConsultar.addActionListener(listener);
    }

    public void setImportAction(ActionListener listener) {
        btnExport.addActionListener(listener);
    }

    public String getSearchText() {
        return txtSearch.getText().trim();
    }
}
