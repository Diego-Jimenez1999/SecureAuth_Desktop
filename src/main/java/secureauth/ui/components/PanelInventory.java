package secureauth.ui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import secureauth.ui.utils.JpanelR;

/**
 * Módulo de Gestión de Inventario - SecureAuth Desktop
 * Implementación optimizada utilizando el componente personalizado JpanelR.
 */
public class PanelInventory extends JPanel {

    // Paleta de colores oficial del proyecto
    private static final Color BG_PAGE = new Color(0xF5F7F9);
    private static final Color BG_CARD = Color.WHITE;
    private static final Color BORDER_COLOR = new Color(0xE5E7EB);
    private static final Color PRIMARY_BLUE = new Color(0x2563EB);
    private static final Color SUCCESS_GREEN = new Color(0x16A34A);
    private static final Color TEXT_DARK = new Color(0x111827);
    private static final Color TEXT_GRAY = new Color(0x6B7280);

    private JTable inventoryTable;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JButton btnAdd;
    private JPanel centerPanel;

    public PanelInventory() {
        setLayout(new BorderLayout(0, 24));
        setBackground(BG_PAGE);
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        centerPanel = new JPanel(new BorderLayout(0, 20));
        centerPanel.setOpaque(false);

        initHeader();
        initStats();
        initTable();
    }

    private void initHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        // Título y Subtítulo
        JPanel titleGroup = new JPanel();
        titleGroup.setLayout(new BoxLayout(titleGroup, BoxLayout.Y_AXIS));
        titleGroup.setOpaque(false);

        JLabel lblTitle = new JLabel("Inventario de Productos");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(TEXT_DARK);

        JLabel lblSub = new JLabel("Control de stock y suministros de la veterinaria");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSub.setForeground(TEXT_GRAY);

        titleGroup.add(lblTitle);
        titleGroup.add(Box.createVerticalStrut(4));
        titleGroup.add(lblSub);

        // Barra de acciones
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        actionPanel.setOpaque(false);

        txtSearch = new JTextField(20);
        txtSearch.putClientProperty("JTextField.placeholderText", "Buscar producto por nombre o ID...");
        txtSearch.setPreferredSize(new Dimension(250, 40));

        btnAdd = new JButton("+ Nuevo Producto");
        btnAdd.setBackground(SUCCESS_GREEN);
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnAdd.setFocusPainted(false);
        btnAdd.setBorderPainted(false);
        btnAdd.setPreferredSize(new Dimension(160, 40));
        btnAdd.setCursor(new Cursor(Cursor.HAND_CURSOR));

        actionPanel.add(txtSearch);
        actionPanel.add(btnAdd);

        headerPanel.add(titleGroup, BorderLayout.WEST);
        headerPanel.add(actionPanel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);
    }

    private void initStats() {
        JPanel statsContainer = new JPanel(new GridLayout(1, 3, 20, 0));
        statsContainer.setOpaque(false);

        statsContainer.add(createStatCard("Total Productos", "124", PRIMARY_BLUE));
        statsContainer.add(createStatCard("Stock Bajo", "8", new Color(0xDC2626)));
        statsContainer.add(createStatCard("Categorías", "12", TEXT_DARK));

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(statsContainer, BorderLayout.CENTER);
        wrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        centerPanel.add(wrapper, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
    }

    private void initTable() {
        // Uso de JpanelR para el contenedor de la tabla
        JpanelR tableCard = new JpanelR();
        tableCard.setBackgroundColor(BG_CARD);
        tableCard.setArc(16);
        tableCard.setBorderConfig(BORDER_COLOR, 1.0f); // Borde sutil para definición
        tableCard.setLayout(new BorderLayout());
        tableCard.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        String[] columns = {"ID", "Producto", "Categoría", "Stock", "Precio", "Estado", "Acciones"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        // Datos de ejemplo
        tableModel.addRow(new Object[]{"#P001", "Alimento ProPlan Adulto", "Comida", "45", "$85.000", "Disponible", "⚙"});
        tableModel.addRow(new Object[]{"#P002", "Shampoo Antipulgas", "Aseo", "4", "$22.500", "Bajo Stock", "⚙"});

        inventoryTable = new JTable(tableModel);
        styleTable(inventoryTable);

        JScrollPane scroll = new JScrollPane(inventoryTable);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(BG_CARD);

        tableCard.add(scroll, BorderLayout.CENTER);

        // Integración en el panel central
        centerPanel.add(tableCard, BorderLayout.CENTER);
    }

    private void styleTable(JTable table) {
        table.setRowHeight(48);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(0xF3F4F6));
        table.setSelectionBackground(new Color(0xEFF6FF));
        table.setSelectionForeground(PRIMARY_BLUE);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(BG_CARD);
        table.getTableHeader().setForeground(TEXT_GRAY);
        table.getTableHeader().setPreferredSize(new Dimension(0, 45));
        ((JLabel)table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.LEFT);
    }

    private JPanel createStatCard(String title, String value, Color accentColor) {
        // Uso de JpanelR para las tarjetas de KPI
        JpanelR card = new JpanelR();
        card.setBackgroundColor(BG_CARD);
        card.setArc(14);
        card.setBorderConfig(BORDER_COLOR, 1.0f);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        card.setPreferredSize(new Dimension(220, 100));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitle.setForeground(TEXT_GRAY);

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblValue.setForeground(accentColor);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);
        
        return card;
    }

    /**
     * Permite enlazar la acción de búsqueda desde el controlador.
     *
     * @param listener listener ejecutado al presionar Enter en el buscador
     */
    public void setSearchAction(ActionListener listener) {
        txtSearch.addActionListener(listener);
    }

    /**
     * Permite enlazar la acción del botón "Nuevo Producto" desde el controlador.
     *
     * @param listener listener ejecutado al hacer clic en el botón
     */
    public void setNewProductAction(ActionListener listener) {
        btnAdd.addActionListener(listener);
    }

    /**
     * @return texto actual del buscador, recortado.
     */
    public String getSearchText() {
        return txtSearch.getText() == null ? "" : txtSearch.getText().trim();
    }
}
