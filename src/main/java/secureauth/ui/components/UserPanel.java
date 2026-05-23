package secureauth.ui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import secureauth.model.Owner;
import secureauth.model.User;
import secureauth.service.OwnerService;
import secureauth.ui.utils.JpanelR;
import secureauth.ui.utils.UiTheme;

/**
 * Panel de gestión de dueños/clientes.
 *
 * <p>Conserva el nombre histórico {@code UserPanel} porque el dashboard lo usa
 * como módulo "Usuarios", pero la responsabilidad actual es administrar dueños
 * de mascotas en la tabla {@code owners}.</p>
 */
public class UserPanel extends JPanel {

    private final OwnerService ownerService;
    private final Runnable onOwnersChanged;

    private JTable table;
    private JTextField txtBuscar;

    public UserPanel(javax.swing.JFrame parentFrame, OwnerService ownerService, Runnable onOwnersChanged) {
        this.ownerService = ownerService;
        this.onOwnersChanged = onOwnersChanged == null ? () -> { } : onOwnersChanged;

        setLayout(new BorderLayout(0, 18));
        setOpaque(false);
        setBorder(new EmptyBorder(8, 0, 0, 0));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildTableCard(), BorderLayout.CENTER);
        loadOwners(null);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);

        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 2));
        titleBox.setOpaque(false);

        JLabel title = new JLabel("Gestión de Dueños");
        title.setFont(UiTheme.TITLE_FONT_SECTION);
        title.setForeground(UiTheme.TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Agrega, edita, elimina y busca clientes para asociarlos a mascotas.");
        subtitle.setFont(UiTheme.BODY_FONT);
        subtitle.setForeground(UiTheme.TEXT_SECONDARY);

        titleBox.add(title);
        titleBox.add(subtitle);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        txtBuscar = new JTextField();
        txtBuscar.setPreferredSize(new Dimension(240, 36));
        txtBuscar.setFont(UiTheme.BODY_FONT);
        txtBuscar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiTheme.BORDER_COLOR),
                new EmptyBorder(6, 10, 6, 10)));
        txtBuscar.addActionListener(e -> loadOwners(txtBuscar.getText()));

        JButton btnBuscar = button("Buscar", 110, UiTheme.BTN_DARK);
        JButton btnNuevo = button("Nuevo dueño", 140, UiTheme.themePrimary());
        JButton btnEditar = button("Editar", 100, UiTheme.BTN_DARK);
        JButton btnEliminar = button("Eliminar", 110, new Color(220, 38, 38));

        btnBuscar.addActionListener(e -> loadOwners(txtBuscar.getText()));
        btnNuevo.addActionListener(e -> openOwnerDialog(null));
        btnEditar.addActionListener(e -> editSelectedOwner());
        btnEliminar.addActionListener(e -> deleteSelectedOwner());

        actions.add(new JLabel("Buscar:"));
        actions.add(txtBuscar);
        actions.add(btnBuscar);
        actions.add(btnNuevo);
        actions.add(btnEditar);
        actions.add(btnEliminar);

        header.add(titleBox, BorderLayout.WEST);
        header.add(actions, BorderLayout.EAST);
        return header;
    }

    private JPanel buildTableCard() {
        JpanelR tablePanel = new JpanelR();
        tablePanel.setBackgroundColor(Color.WHITE);
        tablePanel.setLayout(new BorderLayout());
        tablePanel.setBorder(new EmptyBorder(12, 12, 12, 12));

        String[] columns = {"ID", "Nombre", "Teléfono", "Correo", "Dirección"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(model);
        table.setRowHeight(40);
        table.setFont(UiTheme.BODY_FONT);
        table.setSelectionBackground(new Color(239, 246, 255));
        table.setShowVerticalLines(false);
        table.setGridColor(UiTheme.BORDER_COLOR);
        table.getTableHeader().setBackground(Color.WHITE);
        table.getTableHeader().setFont(UiTheme.SMALL_FONT.deriveFont(Font.BOLD));
        table.getTableHeader().setForeground(UiTheme.TEXT_SECONDARY);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);
        tablePanel.add(scroll, BorderLayout.CENTER);
        return tablePanel;
    }

    private JButton button(String text, int width, Color background) {
        JButton button = new JButton(text);
        button.setFont(UiTheme.SMALL_FONT.deriveFont(Font.BOLD));
        button.setBackground(background);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(width, 36));
        return button;
    }

    public void loadOwners(String query) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);

        List<Owner> owners = ownerService.searchOwners(query);
        for (Owner owner : owners) {
            model.addRow(new Object[]{
                    owner.getId(),
                    safe(owner.getNombreCompleto()),
                    safe(owner.getTelefono()),
                    safe(owner.getCorreo()),
                    safe(owner.getDireccion())
            });
        }
    }

    private void openOwnerDialog(Owner owner) {
        boolean editing = owner != null;

        JTextField name = new JTextField(editing ? safe(owner.getNombreCompleto()) : "");
        JTextField phone = new JTextField(editing ? safe(owner.getTelefono()) : "");
        JTextField email = new JTextField(editing ? safe(owner.getCorreo()) : "");
        JTextField address = new JTextField(editing ? safe(owner.getDireccion()) : "");

        Object[] fields = {
                "Nombre completo *", name,
                "Teléfono *", phone,
                "Correo", email,
                "Dirección", address
        };

        int result = JOptionPane.showConfirmDialog(this, fields,
                editing ? "Editar dueño" : "Nuevo dueño", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            Owner data = editing ? owner : new Owner();
            data.setNombreCompleto(name.getText().trim());
            data.setTelefono(phone.getText().trim());
            data.setCorreo(email.getText().trim());
            data.setDireccion(address.getText().trim());

            if (editing) {
                ownerService.updateOwner(data);
            } else {
                ownerService.createOwner(data);
            }

            loadOwners(txtBuscar.getText());
            onOwnersChanged.run();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Dueños", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void editSelectedOwner() {
        Owner owner = selectedOwner();
        if (owner == null) {
            JOptionPane.showMessageDialog(this, "Selecciona un dueño para editar.");
            return;
        }
        openOwnerDialog(owner);
    }

    private void deleteSelectedOwner() {
        Owner owner = selectedOwner();
        if (owner == null) {
            JOptionPane.showMessageDialog(this, "Selecciona un dueño para eliminar.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Eliminar a " + owner.getNombreCompleto() + "?",
                "Eliminar dueño", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            ownerService.deleteOwner(owner.getId());
            loadOwners(txtBuscar.getText());
            onOwnersChanged.run();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Dueños", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Owner selectedOwner() {
        int row = table.getSelectedRow();
        if (row < 0) {
            return null;
        }
        int modelRow = table.convertRowIndexToModel(row);
        int id = Integer.parseInt(String.valueOf(table.getModel().getValueAt(modelRow, 0)));
        return ownerService.findOwnerById(id);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    public JTable getTable() {
        return table;
    }

    public String getTextoBusqueda() {
        return txtBuscar.getText();
    }

    public void setTextoBusqueda(String texto) {
        txtBuscar.setText(texto == null ? "" : texto);
    }

    /**
     * Compatibilidad con código legado que llamaba loadUsers().
     */
    public void loadUsers(List<User> ignored) {
        loadOwners(getTextoBusqueda());
    }
}
