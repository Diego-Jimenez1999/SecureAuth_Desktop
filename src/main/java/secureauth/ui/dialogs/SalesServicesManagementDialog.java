package secureauth.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import secureauth.domain.sales.SaleItemType;
import secureauth.ui.sales.SalesServiceCatalog;
import secureauth.ui.sales.SalesServiceCatalog.ServiceItemEntry;
import secureauth.ui.utils.UiTheme;

/** Diálogo principal de administración de categorías, subcategorías y servicios/productos.
 *  Permite agregar, editar y eliminar categorías y servicios, así como gestionar precios por tamaño.
 * @version  1.2
 * @author  Diego Jimenez
 */
public class SalesServicesManagementDialog extends JDialog {

    private final SalesServiceCatalog catalog;
    private final DefaultTableModel categoryModel;
    private final DefaultTableModel servicesModel;

    private JTable tblCategorias;
    private JTable tblServicios;
    
    // 
    private JTextField txtCategoria;
    private JTextField txtSubcategoria;
    private JTextField txtNombre;
    private JTextField txtPrecio;
    private JTextField txtCosto;
    private JTextField txtEstado;
    private JTextField txtStock;
    private JTextField txtSizeLabel;
    private JTextField txtSizePrice;
    private JComboBox<SaleItemType> cbTipo;

    private int editingItemId = -1;

    /**
     *  constructor  
     * 
     * @param parent parent del dialogo
     */
    public SalesServicesManagementDialog(JFrame parent) {
        super(parent, "Gestión de Ventas y Servicios", true);
        this.catalog = SalesServiceCatalog.getInstance();
        this.categoryModel = new DefaultTableModel(new String[]{"ID", "Categoría", "Subcategoría"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        this.servicesModel = new DefaultTableModel(
                new String[]{"ID", "Categoría", "Subcategoría", "Nombre", "Tipo", "Precio", "Costo", "Ganancia", "Estado"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        initComponents();
        bindEvents();
        refreshTables();
        catalog.addCatalogListener(evt -> refreshTables());
    }


    /**
    * incicio de los componetes del jdialogo     
    */
    private void initComponents() {
        setSize(1240, 760);
        setLocationRelativeTo(getParent());

        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        root.setBackground(UiTheme.BG_PAGE);

        JLabel title = new JLabel("Gestión de Ventas y Servicios");
        title.setFont(UiTheme.TITLE_FONT_SECTION);
        title.setForeground(UiTheme.TEXT_PRIMARY);
        root.add(title, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildLeftPanel(), buildRightPanel());
        split.setDividerLocation(340);
        split.setBorder(null);
        root.add(split, BorderLayout.CENTER);

        setContentPane(root);
    }

    private JPanel buildLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UiTheme.PANEL_WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UiTheme.BORDER_COLOR),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = baseGbc();

        txtCategoria = field();
        txtSubcategoria = field();

        addField(form, gbc, "Categoría", txtCategoria);
        addField(form, gbc, "Subcategoría", txtSubcategoria);

        JButton btnAgregar = button("Agregar");
        JButton btnEliminar = button("Eliminar");
        btnAgregar.addActionListener(e -> addCategory());
        btnEliminar.addActionListener(e -> removeSelectedCategory());

        gbc.gridx=0;
        gbc.gridy=8;
        gbc.gridwidth=2;
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        actions.setOpaque(false);
        actions.add(btnAgregar);
        actions.add(btnEliminar);
        form.add(actions, gbc);

        tblCategorias = new JTable(categoryModel);
        tblCategorias.setRowHeight(30);
        tblCategorias.setFont(UiTheme.BODY_FONT);

        panel.add(form, BorderLayout.NORTH);
        panel.add(new JScrollPane(tblCategorias), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildRightPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UiTheme.PANEL_WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UiTheme.BORDER_COLOR),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = baseGbc();

        txtNombre = field();
        txtPrecio = field();
        txtCosto = field();
        txtEstado = field("Activo");
        txtStock = field();
        txtSizeLabel = field();
        txtSizePrice = field();
        cbTipo = new JComboBox<>(SaleItemType.values());
        cbTipo.setFont(UiTheme.BODY_FONT);
        cbTipo.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel label = new JLabel(value == null ? "" : value.displayName());
            label.setOpaque(true);
            label.setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
            label.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
            label.setFont(UiTheme.BODY_FONT);
            return label;
        });

        gbc.insets = new Insets(6,6,6,6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        // Fila 1
        gbc.gridx=0; gbc.gridy=0; form.add(new JLabel("Nombre"), gbc);
        gbc.gridx=1; form.add(new JLabel("Tipo"), gbc);
        gbc.gridx=0; gbc.gridy=1; form.add(txtNombre, gbc);
        gbc.gridx=1; form.add(cbTipo, gbc);

        // Fila 2
        gbc.gridx=0; gbc.gridy=2; form.add(new JLabel("Precio"), gbc);
        gbc.gridx=1; form.add(new JLabel("Costo"), gbc);
        gbc.gridx=0; gbc.gridy=3; form.add(txtPrecio, gbc);
        gbc.gridx=1; form.add(txtCosto, gbc);

        // Fila 3
        gbc.gridx=0; gbc.gridy=4; form.add(new JLabel("Estado"), gbc);
        gbc.gridx=1; form.add(new JLabel("Stock (opcional)"), gbc);
        gbc.gridx=0; gbc.gridy=5; form.add(txtEstado, gbc);
        gbc.gridx=1; form.add(txtStock, gbc);

        // Fila 4
        gbc.gridx=0; gbc.gridy=6; form.add(new JLabel("Tamaño"), gbc);
        gbc.gridx=1; form.add(new JLabel("Precio tamaño"), gbc);
        gbc.gridx=0; gbc.gridy=7; form.add(txtSizeLabel, gbc);
        gbc.gridx=1; form.add(txtSizePrice, gbc);

        JButton btnGuardar = button("Guardar");
        JButton btnEditar = button("Cargar Selección");
        JButton btnEliminar = button("Eliminar");
        JButton btnPrecios = button("Precios por Tamaño");
        btnGuardar.addActionListener(e -> saveItem());
        btnEditar.addActionListener(e -> loadSelectedService());
        btnEliminar.addActionListener(e -> removeSelectedService());
        btnPrecios.addActionListener(e -> openSizePricesDialog());

        gbc.gridx=0;
        gbc.gridy=8;
        gbc.gridwidth=2;
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        actions.setOpaque(false);
        actions.add(btnGuardar);
        actions.add(btnEditar);
        actions.add(btnEliminar);
        actions.add(btnPrecios);
        form.add(actions, gbc);

        tblServicios = new JTable(servicesModel);
        tblServicios.setRowHeight(30);
        tblServicios.setFont(UiTheme.BODY_FONT);

        panel.add(form, BorderLayout.NORTH);
        panel.add(new JScrollPane(tblServicios), BorderLayout.CENTER);
        return panel;
    }

    private void bindEvents() {
        tblCategorias.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                return;
            }
            int row = tblCategorias.getSelectedRow();
            if (row >= 0) {
                txtCategoria.setText(String.valueOf(categoryModel.getValueAt(row, 1)));
                txtSubcategoria.setText(String.valueOf(categoryModel.getValueAt(row, 2)));
            }
        });
    }

    private void refreshTables() {
        categoryModel.setRowCount(0);
        for (SalesServiceCatalog.CategoryEntry c : catalog.getCategories()) {
            categoryModel.addRow(new Object[]{c.id(), c.category(), c.subcategory()});
        }

        servicesModel.setRowCount(0);
        for (ServiceItemEntry item : catalog.getItems()) {
            servicesModel.addRow(new Object[]{item.id(), item.category(), item.subcategory(), item.name(), item.type(),
                    item.price(), item.cost(), item.gain(), item.status()});
        }
    }

    private void addCategory() {
        if (txtCategoria.getText().isBlank() || txtSubcategoria.getText().isBlank()) {
            warn("Categoría y subcategoría son obligatorias.");
            return;
        }
        catalog.addCategory(txtCategoria.getText().trim(), txtSubcategoria.getText().trim());
    }

    private void removeSelectedCategory() {
        int row = tblCategorias.getSelectedRow();
        if (row < 0) {
            warn("Selecciona una categoría para eliminar.");
            return;
        }
        int id = Integer.parseInt(String.valueOf(categoryModel.getValueAt(row, 0)));
        catalog.removeCategory(id);
    }

    private void saveItem() {
        int catRow = tblCategorias.getSelectedRow();
        if (catRow < 0) {
            warn("Selecciona una categoría/subcategoría en la sección izquierda.");
            return;
        }
        if (txtNombre.getText().isBlank()) {
            warn("El nombre es obligatorio.");
            return;
        }
        String category = String.valueOf(categoryModel.getValueAt(catRow, 1));
        String subcategory = String.valueOf(categoryModel.getValueAt(catRow, 2));
        double price = parseDouble(txtPrecio.getText());
        double cost = parseDouble(txtCosto.getText());
        Integer stock = txtStock.getText().isBlank() ? null : (int) parseDouble(txtStock.getText());

        Map<String, Double> sizePrices = new LinkedHashMap<>();
        if (!txtSizeLabel.getText().isBlank() && !txtSizePrice.getText().isBlank()) {
            sizePrices.put(txtSizeLabel.getText().trim(), parseDouble(txtSizePrice.getText()));
        }

        ServiceItemEntry entry = new ServiceItemEntry(editingItemId, category, subcategory, txtNombre.getText().trim(),
                selectedType().displayName(), price, cost, price - cost,
                txtEstado.getText().isBlank() ? "Activo" : txtEstado.getText().trim(), stock, sizePrices);
        catalog.upsertItem(entry);
        clearForm();
    }

    private void loadSelectedService() {
        int row = tblServicios.getSelectedRow();
        if (row < 0) {
            warn("Selecciona un servicio/producto para editar.");
            return;
        }
        editingItemId = Integer.parseInt(String.valueOf(servicesModel.getValueAt(row, 0)));
        txtNombre.setText(String.valueOf(servicesModel.getValueAt(row, 3)));
        cbTipo.setSelectedItem(SaleItemType.fromCatalogValue(String.valueOf(servicesModel.getValueAt(row, 4))));
        txtPrecio.setText(String.valueOf(servicesModel.getValueAt(row, 5)));
        txtCosto.setText(String.valueOf(servicesModel.getValueAt(row, 6)));
        txtEstado.setText(String.valueOf(servicesModel.getValueAt(row, 8)));
    }

    private void removeSelectedService() {
        int row = tblServicios.getSelectedRow();
        if (row < 0) {
            warn("Selecciona un servicio/producto para eliminar.");
            return;
        }
        int id = Integer.parseInt(String.valueOf(servicesModel.getValueAt(row, 0)));
        catalog.removeItem(id);
        clearForm();
    }

    private void openSizePricesDialog() {
        new SizePricesDialog((JFrame) getParent()).setVisible(true);
    }

    private void clearForm() {
        editingItemId = -1;
        txtNombre.setText("");
        txtPrecio.setText("");
        txtCosto.setText("");
        txtEstado.setText("Activo");
        txtStock.setText("");
        txtSizeLabel.setText("");
        txtSizePrice.setText("");
    }

    private JTextField field() { return field(""); }

    private JTextField field(String value) {
        JTextField field = new JTextField(value);
        field.setPreferredSize(new Dimension(290, 34));
        field.setFont(UiTheme.BODY_FONT);
        return field;
    }

    private JButton button(String text) {
        JButton button = new JButton(text);
        UiTheme.styleButton(button, UiTheme.BTN_DARK, UiTheme.BTN_DARK_HOVER, UiTheme.TEXT_LIGHT,
                150, 34, 12, true, false, 8);
        return button;
    }

    private void addField(JPanel panel, GridBagConstraints gbc, String label, java.awt.Component component) {
        gbc.gridy++;
        panel.add(new JLabel(label), gbc);
        gbc.gridy++;
        panel.add(component, gbc);
    }

    private GridBagConstraints baseGbc() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = -1;
        gbc.insets = new Insets(4, 0, 4, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        return gbc;
    }

    private double parseDouble(String value) {
        try {
            return Double.parseDouble(value.trim().replace(',', '.'));
        } catch (NumberFormatException ex) {
            return 0d;
        }
    }

    private SaleItemType selectedType() {
        Object selected = cbTipo.getSelectedItem();
        return selected instanceof SaleItemType type ? type : SaleItemType.PRODUCT;
    }

    private void warn(String message) {
        JOptionPane.showMessageDialog(this, message, "Validación", JOptionPane.WARNING_MESSAGE);
    }
}
