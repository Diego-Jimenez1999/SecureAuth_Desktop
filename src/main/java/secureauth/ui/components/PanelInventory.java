package secureauth.ui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
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
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import secureauth.model.enterprise.InventoryItem;

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
        setBackground(new Color(243, 245, 247));
        setBorder(new EmptyBorder(25, 25, 25, 25));

        // =====================================================
        // HEADER
        // =====================================================

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Módulo de Inventario");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setForeground(new Color(25, 25, 25));

        JLabel lblSubtitle = new JLabel("Dashboard de control y gestión de productos");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSubtitle.setForeground(new Color(120, 120, 120));

        titlePanel.add(lblTitle);
        titlePanel.add(Box.createVerticalStrut(4));
        titlePanel.add(lblSubtitle);

        // =====================================================
        // ACTIONS
        // =====================================================

        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actionsPanel.setOpaque(false);

        txtSearch = new JTextField();
        txtSearch.setPreferredSize(new Dimension(220, 42));
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220,220,220)),
                new EmptyBorder(0, 12, 0, 12)
        ));

        btnConsultar = createButton("Generar Reporte", true);
        btnExport = createButton("Exportar", false);

        actionsPanel.add(txtSearch);
        actionsPanel.add(btnConsultar);
        actionsPanel.add(btnExport);

        header.add(titlePanel, BorderLayout.WEST);
        header.add(actionsPanel, BorderLayout.EAST);

        // =====================================================
        // METRIC CARDS
        // =====================================================

        JPanel metricsPanel = new JPanel(new GridLayout(1, 4, 18, 0));
        metricsPanel.setOpaque(false);

        JPanel card1 = createMetricCard(
                "Total Productos",
                "0",
                "Productos registrados",
                new Color(0, 170, 90)
        );

        JPanel card2 = createMetricCard(
                "Stock Bajo",
                "0",
                "Requieren reposición",
                new Color(220, 53, 69)
        );

        JPanel card3 = createMetricCard(
                "Categorías",
                "12",
                "Categorías activas",
                new Color(0, 123, 255)
        );

        JPanel card4 = createMetricCard(
                "Valor Total",
                "$0",
                "Valor inventario",
                new Color(33, 37, 41)
        );

        lblTotalProducts = getMetricValue(card1);
        lblLowStock = getMetricValue(card2);

        metricsPanel.add(card1);
        metricsPanel.add(card2);
        metricsPanel.add(card3);
        metricsPanel.add(card4);

        // =====================================================
        // TABLE PANEL
        // =====================================================

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225,225,225)),
                new EmptyBorder(15,15,15,15)
        ));

        JLabel lblTable = new JLabel("Resumen de Inventario");
        lblTable.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTable.setBorder(new EmptyBorder(0, 0, 15, 0));

        String[] columns = {
                "SKU",
                "Producto",
                "Categoría",
                "Stock",
                "Mínimo",
                "Estado"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        inventoryTable = new JTable(tableModel);
        styleTable();

        JScrollPane scrollPane = new JScrollPane(inventoryTable);
        scrollPane.setBorder(null);

        tablePanel.add(lblTable, BorderLayout.NORTH);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // =====================================================
        // CENTER CONTENT
        // =====================================================

        JPanel center = new JPanel(new BorderLayout(0, 20));
        center.setOpaque(false);

        center.add(metricsPanel, BorderLayout.NORTH);
        center.add(tablePanel, BorderLayout.CENTER);

        add(header, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
    }

    // =====================================================
    // MODERN BUTTON
    // =====================================================

    private JButton createButton(String text, boolean dark) {

        JButton btn = new JButton(text);

        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setPreferredSize(new Dimension(160, 42));
        btn.setBorder(new EmptyBorder(10, 18, 10, 18));

        if (dark) {
            btn.setBackground(new Color(24, 28, 34));
            btn.setForeground(Color.WHITE);
        } else {
            btn.setBackground(Color.WHITE);
            btn.setForeground(new Color(30, 30, 30));
            btn.setBorder(BorderFactory.createLineBorder(new Color(220,220,220)));
        }

        return btn;
    }

    // =====================================================
    // MODERN METRIC CARD
    // =====================================================

    private JPanel createMetricCard(
            String title,
            String value,
            String subtitle,
            Color valueColor
    ) {

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        card.setBackground(Color.WHITE);

        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230,230,230)),
                new EmptyBorder(18,18,18,18)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblTitle.setForeground(new Color(120,120,120));

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblValue.setForeground(valueColor);

        JLabel lblSub = new JLabel(subtitle);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(160,160,160));

        card.add(lblTitle);
        card.add(Box.createVerticalStrut(12));
        card.add(lblValue);
        card.add(Box.createVerticalStrut(6));
        card.add(lblSub);

        return card;
    }

    private JLabel getMetricValue(JPanel panel) {
        return (JLabel) panel.getComponent(2);
    }

    // =====================================================
    // TABLE STYLE
    // =====================================================

    private void styleTable() {

        inventoryTable.setRowHeight(42);

        inventoryTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        inventoryTable.setSelectionBackground(new Color(235, 240, 245));

        inventoryTable.setGridColor(new Color(240,240,240));

        inventoryTable.setShowVerticalLines(false);

        JTableHeader header = inventoryTable.getTableHeader();

        header.setBackground(new Color(28, 33, 39));
        header.setForeground(Color.WHITE);

        header.setFont(new Font("Segoe UI", Font.BOLD, 13));

        header.setPreferredSize(new Dimension(100, 42));

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);

        for (int i = 0; i < inventoryTable.getColumnCount(); i++) {
            inventoryTable.getColumnModel().getColumn(i).setCellRenderer(center);
        }
    }

    // =====================================================
    // RENDER DATA
    // =====================================================

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

            tableModel.addRow(new Object[]{
                    item.sku(),
                    item.name(),
                    item.category(),
                    item.stock(),
                    item.minStock(),
                    status
            });
        }

        lblTotalProducts.setText(String.valueOf(items.size()));
        lblLowStock.setText(String.valueOf(lowStock));
    }

    // =====================================================
    // ACTIONS
    // =====================================================

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