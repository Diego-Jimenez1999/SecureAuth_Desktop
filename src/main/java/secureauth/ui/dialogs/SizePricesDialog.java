package secureauth.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
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
import javax.swing.JTextField;

import secureauth.domain.sales.ServiceItemEntry;
import secureauth.service.enterprise.SalesServiceCatalog;
import secureauth.ui.utils.UiTheme;

/** Diálogo para editar precios dinámicos por tamaño en servicios/productos. */
public class SizePricesDialog extends JDialog {

    private final SalesServiceCatalog catalog;
    private final JComboBox<ServiceItemEntry> cbItems;
    private final JTextField txtTamano;
    private final JTextField txtPrecio;

    public SizePricesDialog(JFrame parent) {
        super(parent, "Precios por Tamaño", true);
        this.catalog = SalesServiceCatalog.getInstance();
        this.cbItems = new JComboBox<>(catalog.getItems().toArray(new ServiceItemEntry[0]));
        this.txtTamano = new JTextField();
        this.txtPrecio = new JTextField();
        init();
    }

    private void init() {
        setSize(560, 260);
        setLocationRelativeTo(getParent());

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(UiTheme.PANEL_WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel form = new JPanel(new java.awt.GridLayout(6, 1, 0, 6));
        form.setOpaque(false);
        form.add(new JLabel("Servicio/Producto"));
        cbItems.setPreferredSize(new Dimension(300, 30));
        cbItems.setRenderer((list, value, index, isSelected, cellHasFocus) -> new JLabel(value == null ? "" : value.name()));
        form.add(cbItems);
        form.add(new JLabel("Tamaño"));
        form.add(txtTamano);
        form.add(new JLabel("Precio"));
        form.add(txtPrecio);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JButton btnGuardar = new JButton("Guardar");
        UiTheme.styleButton(btnGuardar, UiTheme.BTN_DARK, UiTheme.BTN_DARK_HOVER, UiTheme.TEXT_LIGHT,
                120, 34, 12, true, false, 8);
        btnGuardar.addActionListener(e -> save());
        actions.add(btnGuardar);

        panel.add(form, BorderLayout.CENTER);
        panel.add(actions, BorderLayout.SOUTH);
        setContentPane(panel);
    }

    private void save() {
        ServiceItemEntry selected = (ServiceItemEntry) cbItems.getSelectedItem();
        if (selected == null || txtTamano.getText().isBlank() || txtPrecio.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "Completa todos los campos.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Map<String, Double> sizes = new LinkedHashMap<>(selected.sizePrices());
        sizes.put(txtTamano.getText().trim(), parsePrice(txtPrecio.getText()));

        ServiceItemEntry updated = new ServiceItemEntry(selected.id(), selected.category(), selected.subcategory(),
                selected.name(), selected.type(), selected.price(), selected.cost(), selected.gain(), selected.status(),
                selected.stock(), sizes);
        catalog.upsertItem(updated);
        dispose();
    }

    private double parsePrice(String value) {
        try {
            return Double.parseDouble(value.trim().replace(',', '.'));
        } catch (Exception ex) {
            return 0d;
        }
    }
}
