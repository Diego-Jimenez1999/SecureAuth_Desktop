package secureauth.ui.components.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import secureauth.controller.IngresoController;

/**
 * Editor de la columna de acciones (editar/eliminar) en la tabla de usuarios.
 *
 * @author Diego
 * @version 1.0
 */
public class ActionCellEditor extends DefaultCellEditor {

    private final JPanel panel;
    private final JButton btnEditar;
    private final JButton btnEliminar;
    private final JTable table;
    private final JFrame parentFrame;
    private final IngresoController controller;

    private int row;

    /**
     * Constructor del editor de celdas.
     *
     * @param checkBox componente requerido por DefaultCellEditor
     * @param parent frame padre para diálogos
     * @param controller controlador para eventos de editar/eliminar
     * @param table tabla asociada
     */
    public ActionCellEditor(JCheckBox checkBox, JFrame parent, IngresoController controller, JTable table) {
        super(checkBox);

        this.parentFrame = parent;
        this.controller = controller;
        this.table = table;

        panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));

        btnEditar = new JButton("✏");
        btnEliminar = new JButton("🗑");

        btnEditar.setBackground(new Color(33, 150, 243));
        btnEditar.setForeground(Color.WHITE);

        btnEliminar.setBackground(new Color(198, 40, 40));
        btnEliminar.setForeground(Color.WHITE);

        btnEditar.addActionListener(e -> editarUsuario());
        btnEliminar.addActionListener(e -> eliminarUsuario());

        panel.add(btnEditar);
        panel.add(btnEliminar);
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        this.row = row;
        return panel;
    }

    private void editarUsuario() {
        fireEditingStopped();

        int id = (int) table.getValueAt(row, 0);
        controller.editarUsuario(id);
    }

    private void eliminarUsuario() {
        fireEditingStopped();

        int id = (int) table.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(
                parentFrame,
                "¿Seguro que deseas eliminar este usuario?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm == JOptionPane.YES_OPTION) {
            controller.eliminarUsuario(id);
        }
    }
}
