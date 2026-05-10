package secureauth.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import secureauth.model.SaleItem;

/**
 * Diálogo modal para seleccionar un sub-servicio.
 */
public class SubServiceDialog extends JDialog {

    private SaleItem selectedItem;

    /**
     * Construye el diálogo modal centrado en la ventana padre.
     *
     * @param parent ventana padre para centrado y modalidad
     * @param serviceName nombre del servicio principal
     * @param subServices lista de sub-servicios disponibles
     */
    public SubServiceDialog(java.awt.Window parent, String serviceName, List<SaleItem> subServices) {
        super(parent, "Subcategorías - " + serviceName, Dialog.ModalityType.APPLICATION_MODAL);
        setLayout(new BorderLayout(10, 10));
        setPreferredSize(new Dimension(390, 320));

        DefaultListModel<SaleItem> model = new DefaultListModel<>();
        for (SaleItem item : subServices) {
            model.addElement(item);
        }

        JList<SaleItem> list = new JList<>(model);
        list.setCellRenderer(new SubServiceRenderer());
        add(new JScrollPane(list), BorderLayout.CENTER);

        JButton confirmButton = new JButton("Confirmar");
        confirmButton.addActionListener(e -> {
            selectedItem = list.getSelectedValue();
            dispose();
        });

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.add(confirmButton);
        add(footer, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(parent);
    }

    /**
     * Retorna el sub-servicio seleccionado.
     *
     * @return servicio elegido o {@code null} si el usuario no confirmó
     */
    public SaleItem getSelectedItem() {
        return selectedItem;
    }

    private static class SubServiceRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                boolean cellHasFocus) {
            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof SaleItem item) {
                setText("<html><b>" + item.getName() + "</b><br><span style='color:#777777;'>$"
                        + String.format("%,.0f", item.getPrice()) + "</span></html>");
            }
            return c;
        }
    }
}
