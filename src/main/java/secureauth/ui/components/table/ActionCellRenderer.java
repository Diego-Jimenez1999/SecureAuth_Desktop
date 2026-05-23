package secureauth.ui.components.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import secureauth.ui.utils.UiTheme;

/**
 * Renderer de la columna de acciones (editar/eliminar) en la tabla de usuarios.
 *
 * @author Diego
 * @version 1.0
 */
public class ActionCellRenderer extends JPanel implements TableCellRenderer {

    private final JButton btnEditar = new JButton("✏");
    private final JButton btnEliminar = new JButton("🗑");

    /**
     * Inicializa renderer con estilos visuales.
     */
    public ActionCellRenderer() {
        setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));

        btnEditar.setBackground(new Color(33, 150, 243));
        btnEditar.setForeground(Color.WHITE);

        btnEliminar.setBackground(UiTheme.themeTertiary());
        btnEliminar.setForeground(Color.WHITE);

        add(btnEditar);
        add(btnEliminar);
    }

    @Override
    public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        return this;
    }
}
