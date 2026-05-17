package secureauth.ui.enterprise;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import secureauth.events.AppEventBus;
import secureauth.events.ClientCreatedEvent;
import secureauth.model.Owner;
import secureauth.service.OwnerService;
import secureauth.ui.utils.UiTheme;
import secureauth.ui.utils.factory.ButtonFactory;
import secureauth.ui.utils.factory.TableFactory;

/** Módulo de clientes (dueños) con alta rápida y búsqueda. */
public class ClientsPanel extends JPanel {

    private final OwnerService ownerService = new OwnerService(new secureauth.dao.OwnerDAO());
    private final DefaultTableModel model;
    private final JTable table;
    private final JTextField txtSearch;

    public ClientsPanel() {
        setLayout(new BorderLayout(0, 10));
        setBackground(UiTheme.BG_PAGE);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel title = new JLabel("Clientes");
        title.setFont(UiTheme.TITLE_FONT_SECTION);
        title.setForeground(UiTheme.TEXT_PRIMARY);

        txtSearch = new JTextField(20);
        JButton btnSearch = ButtonFactory.dark("Buscar", 130);
        JButton btnNew = ButtonFactory.primary("Nuevo Cliente", 130);
        btnSearch.addActionListener(e -> load(txtSearch.getText()));
        btnNew.addActionListener(e -> createClient());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        top.setOpaque(false);
        top.add(title);
        top.add(txtSearch);
        top.add(btnSearch);
        top.add(btnNew);

        model = new DefaultTableModel(new String[]{"ID", "Nombre", "Teléfono", "Correo", "Dirección"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(model);
        TableFactory.applyEnterpriseStyle(table);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        load(null);
    }

    private void load(String query) {
        model.setRowCount(0);
        List<Owner> owners = ownerService.findAllOwners();
        String q = query == null ? "" : query.trim().toLowerCase();
        for (Owner owner : owners) {
            if (!q.isEmpty() && !owner.getNombreCompleto().toLowerCase().contains(q) && !owner.getCorreo().toLowerCase().contains(q)) {
                continue;
            }
            model.addRow(new Object[]{owner.getId(), owner.getNombreCompleto(), owner.getTelefono(), owner.getCorreo(), owner.getDireccion()});
        }
    }

    private void createClient() {
        JTextField name = new JTextField();
        JTextField phone = new JTextField();
        JTextField email = new JTextField();
        JTextField address = new JTextField();
        Object[] fields = {"Nombre", name, "Teléfono", phone, "Correo", email, "Dirección", address};
        if (JOptionPane.showConfirmDialog(this, fields, "Nuevo Cliente", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
            return;
        }
        if (name.getText().isBlank() || email.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "Nombre y correo son obligatorios.");
            return;
        }
        Owner owner = new Owner(0, name.getText().trim(), phone.getText().trim(), email.getText().trim(), address.getText().trim());
        ownerService.createOwner(owner);
        AppEventBus.getInstance().publish(new ClientCreatedEvent(0, owner.getNombreCompleto()));
        load(null);
    }
}
