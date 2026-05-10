package secureauth.ui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import secureauth.controller.IngresoController;
import secureauth.model.User;
import secureauth.ui.components.table.ActionCellEditor;
import secureauth.ui.components.table.ActionCellRenderer;
import secureauth.ui.utils.JpanelR;

/**
 * Panel dinámico de usuarios para renderizarse dentro del CardLayout principal.
 */
public class UserPanel extends JPanel {

    private final IngresoController controller;
    private JTable table;
    private JTextField txtBuscar;

    public UserPanel(javax.swing.JFrame parentFrame, IngresoController controller) {
        this.controller = controller;
        setLayout(new BorderLayout(0, 20));
        setOpaque(false);
        setBorder(new EmptyBorder(8, 0, 0, 0));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildTableCard(parentFrame), BorderLayout.CENTER);
        add(buildQuickSearchPanel(), BorderLayout.SOUTH);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Registro de Usuarios");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));

        JPanel searchBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        searchBox.setOpaque(false);

        txtBuscar = new JTextField();
        txtBuscar.setPreferredSize(new Dimension(220, 34));
        txtBuscar.setFont(new Font("SansSerif", Font.PLAIN, 14));
        txtBuscar.addActionListener(e -> controller.buscarUsuarios());

        JButton btnRefresh = new JButton(" ⟳ ");
        btnRefresh.setBackground(new Color(30, 36, 48));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFocusPainted(false);
        btnRefresh.setPreferredSize(new Dimension(55, 34));
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRefresh.addActionListener(e -> controller.cargarUsuarios());

        searchBox.add(new JLabel("Buscar:"));
        searchBox.add(txtBuscar);
        searchBox.add(btnRefresh);

        header.add(title, BorderLayout.WEST);
        header.add(searchBox, BorderLayout.EAST);
        return header;
    }

    private JPanel buildTableCard(javax.swing.JFrame parentFrame) {
        JpanelR tablePanel = new JpanelR();
        tablePanel.setBackgroundColor(Color.WHITE);
        tablePanel.setLayout(new BorderLayout());
        tablePanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        String[] columns = {"ID", "Nombre", "Email", "Género", "Acción"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);

        table = new JTable(model);
        table.setRowHeight(45);
        table.setSelectionBackground(new Color(240, 240, 240));
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(230, 230, 230));
        table.getTableHeader().setBackground(Color.WHITE);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        table.getTableHeader().setForeground(Color.GRAY);
        table.getColumn("Acción").setCellRenderer(new ActionCellRenderer());
        table.getColumn("Acción").setCellEditor(new ActionCellEditor(new JCheckBox(), parentFrame, controller, table));

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);
        tablePanel.add(scroll, BorderLayout.CENTER);
        return tablePanel;
    }

    private JPanel buildQuickSearchPanel() {
        return new SearchPanel(texto -> {
            setTextoBusqueda(texto);
            controller.buscarUsuarios();
        });
    }

    /**
     * Carga los usuarios en la tabla. Este método permite reutilizar el panel sin recrearlo.
     */
    public void loadUsers(java.util.List<User> users) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);
        for (User u : users) {
            model.addRow(new Object[]{
                    u.getId(),
                    u.getNombre() + " " + u.getApellido(),
                    u.getEmail(),
                    u.getGenero(),
                    "Editar | Eliminar"
            });
        }
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
}
