package secureauth.ui.components;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import secureauth.model.enterprise.InventoryItem;
import secureauth.ui.utils.UiTheme;

/** Panel enterprise de inventario multi-sucursal. */
public class PanelInventory extends JPanel {

    private final DefaultTableModel tableModel;
    private final JTable inventoryTable;
    private final JTextField txtSearch;
    private final JButton btnConsultar;
    private final JButton btnImport;
    private final JLabel lblTotal;
    private final JLabel lblLow;

    public PanelInventory() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UiTheme.BG_PAGE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel title = new JLabel("Inventario Integrado");
        title.setFont(UiTheme.TITLE_FONT_SECTION);
        title.setForeground(UiTheme.TEXT_PRIMARY);
        top.add(title, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        txtSearch = new JTextField(20);
        txtSearch.setPreferredSize(new Dimension(220, 34));
        txtSearch.setFont(UiTheme.BODY_FONT);
        btnConsultar = new JButton("Ver Inventario");
        btnImport = new JButton("Importar CSV/Excel");
        UiTheme.styleButton(btnConsultar, UiTheme.BTN_DARK, UiTheme.BTN_DARK_HOVER, UiTheme.TEXT_LIGHT, 140, 34, 12, true, false, 8);
        UiTheme.styleButton(btnImport, UiTheme.BTN_DARK, UiTheme.BTN_DARK_HOVER, UiTheme.TEXT_LIGHT, 180, 34, 12, true, false, 8);
        actions.add(txtSearch);
        actions.add(btnConsultar);
        actions.add(btnImport);
        top.add(actions, BorderLayout.EAST);

        JPanel stats = new JPanel(new GridLayout(1, 2, 8, 8));
        stats.setOpaque(false);
        lblTotal = metric("Total Productos", "0");
        lblLow = metric("Alertas Stock", "0");
        stats.add(wrap(lblTotal));
        stats.add(wrap(lblLow));

        String[] columns = {"SKU", "Nombre", "Categoría", "Stock", "Mínimo", "Proveedor", "Costo", "Precio", "Estado"};
        tableModel = new DefaultTableModel(columns, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        inventoryTable = new JTable(tableModel);
        inventoryTable.setRowHeight(28);
        inventoryTable.setFont(UiTheme.BODY_FONT);

        add(top, BorderLayout.NORTH);
        add(stats, BorderLayout.CENTER);
        add(new JScrollPane(inventoryTable), BorderLayout.SOUTH);
    }

    public void renderItems(List<InventoryItem> items) {
        tableModel.setRowCount(0);
        int low = 0;
        for (InventoryItem item : items) {
            if (item.stock() <= item.minStock()) low++;
            tableModel.addRow(new Object[]{item.sku(), item.name(), item.category(), item.stock(), item.minStock(), item.supplier(), item.cost(), item.price(), item.status()});
        }
        lblTotal.setText("Total Productos: " + items.size());
        lblLow.setText("Alertas Stock: " + low);
    }

    public void setSearchAction(ActionListener listener) {
        txtSearch.addActionListener(listener);
        btnConsultar.addActionListener(listener);
    }

    public void setImportAction(ActionListener listener) {
        btnImport.addActionListener(listener);
    }

    public String getSearchText() {
        return txtSearch.getText() == null ? "" : txtSearch.getText().trim();
    }

    private JLabel metric(String title, String value) {
        JLabel label = new JLabel(title + ": " + value);
        label.setFont(UiTheme.BODY_FONT.deriveFont(java.awt.Font.BOLD));
        label.setForeground(UiTheme.TEXT_PRIMARY);
        return label;
    }

    private JPanel wrap(JLabel label) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(UiTheme.PANEL_WHITE);
        panel.setBorder(BorderFactory.createLineBorder(UiTheme.BORDER_COLOR));
        panel.add(label);
        return panel;
    }
}
