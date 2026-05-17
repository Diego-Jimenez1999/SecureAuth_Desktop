package secureauth.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;

import secureauth.service.enterprise.InventoryService;
import secureauth.ui.utils.factory.ButtonFactory;

/** Wizard de importación inteligente con mapeo de columnas y preview. */
public class InventoryImportWizardDialog extends JDialog {

    private final InventoryService service;
    private final File file;
    private final InventoryService.RawImportData raw;
    private InventoryService.ImportPlan plan;

    private final Map<String, JComboBox<String>> mappingBoxes = new HashMap<>();
    private final DefaultTableModel previewModel = new DefaultTableModel();
    private final JTextArea errorsArea = new JTextArea();

    public InventoryImportWizardDialog(JFrame parent, InventoryService service, File file) throws Exception {
        super(parent, "Importador Inventario", true);
        this.service = service;
        this.file = file;
        this.raw = service.readRawImport(file);
        init();
    }

    private void init() {
        setSize(1000, 640);
        setLocationRelativeTo(getParent());

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel mappingPanel = new JPanel(new java.awt.GridLayout(0, 2, 8, 8));
        List<String> headers = raw.headers();
        for (String field : InventoryService.SUPPORTED_FIELDS) {
            JComboBox<String> box = new JComboBox<>();
            DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
            model.addElement("-- no mapear --");
            for (String h : headers) model.addElement(h);
            box.setModel(model);
            int idx = headers.indexOf(field);
            box.setSelectedIndex(idx >= 0 ? idx + 1 : 0);
            mappingBoxes.put(field, box);
            mappingPanel.add(new JLabel(field));
            mappingPanel.add(box);
        }

        JButton btnPreview = ButtonFactory.dark("Validar Preview", 150);
        JButton btnImport = ButtonFactory.primary("Importar", 120);
        btnPreview.addActionListener(e -> buildPreview());
        btnImport.addActionListener(e -> doImport());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(btnPreview);
        actions.add(btnImport);

        JTable previewTable = new JTable(previewModel);
        JScrollPane previewScroll = new JScrollPane(previewTable);
        previewScroll.setPreferredSize(new Dimension(980, 280));

        errorsArea.setEditable(false);
        JScrollPane errorScroll = new JScrollPane(errorsArea);
        errorScroll.setPreferredSize(new Dimension(980, 120));

        root.add(mappingPanel, BorderLayout.NORTH);
        root.add(previewScroll, BorderLayout.CENTER);
        root.add(errorScroll, BorderLayout.SOUTH);
        root.add(actions, BorderLayout.EAST);

        setContentPane(root);
    }

    private void buildPreview() {
        Map<String, Integer> mapping = new HashMap<>();
        for (Map.Entry<String, JComboBox<String>> entry : mappingBoxes.entrySet()) {
            int index = entry.getValue().getSelectedIndex();
            if (index > 0) {
                mapping.put(entry.getKey(), index - 1);
            }
        }

        plan = service.buildImportPlan(raw, mapping);

        previewModel.setRowCount(0);
        previewModel.setColumnIdentifiers(new String[]{"Código", "Nombre", "Categoría", "Stock", "Stock Min", "Proveedor", "Costo", "Precio"});
        for (InventoryService.ImportRow row : plan.validRows()) {
            previewModel.addRow(new Object[]{row.codigo(), row.nombre(), row.categoria(), row.stock(), row.stockMinimo(), row.proveedor(), row.costo(), row.precio()});
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Archivo: ").append(file.getName()).append("\n");
        sb.append("Filas válidas: ").append(plan.validRows().size()).append("\n");
        sb.append("Duplicados por SKU (omitidos): ").append(plan.duplicates()).append("\n");
        if (!plan.errors().isEmpty()) {
            sb.append("Errores:\n");
            for (String e : plan.errors()) sb.append(" - ").append(e).append("\n");
        }
        errorsArea.setText(sb.toString());
    }

    private void doImport() {
        if (plan == null) {
            JOptionPane.showMessageDialog(this, "Primero valida el preview.");
            return;
        }
        if (plan.validRows().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay filas válidas para importar.");
            return;
        }
        try {
            service.importRows(plan.validRows());
            JOptionPane.showMessageDialog(this, "Importación completada: " + plan.validRows().size() + " filas.");
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error importando: " + ex.getMessage());
        }
    }
}
