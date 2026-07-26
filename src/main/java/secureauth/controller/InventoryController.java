package secureauth.controller;

import java.io.File;
import java.sql.SQLException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import secureauth.model.enterprise.InventoryItem;
import secureauth.service.enterprise.InventoryService;
import secureauth.ui.components.PanelInventory;
import secureauth.ui.sales.SalesServiceCatalog;

/** Controlador enterprise del inventario con importación CSV/XLSX. */
public class InventoryController {

    private final PanelInventory view;
    private final InventoryService service;

    public InventoryController(PanelInventory view) {
        this(view, new InventoryService());
    }

    public InventoryController(PanelInventory view, InventoryService service) {
        this.view = view;
        this.service = service;
        bindActions();
        initialize();
    }

    private void initialize() {
        new javax.swing.SwingWorker<Void, Void>() {
            private String errorMsg = null;

            @Override
            protected Void doInBackground() throws Exception {
                try {
                    service.initializeSchema();
                } catch (SQLException ex) {
                    errorMsg = ex.getMessage();
                }
                return null;
            }

            @Override
            protected void done() {
                if (errorMsg != null) {
                    JOptionPane.showMessageDialog(view, "No se pudo inicializar inventario: " + errorMsg);
                }
                loadInventory();
            }
        }.execute();
    }

    private void bindActions() {
        view.setSearchAction(e -> loadInventory());
        view.setImportAction(e -> importFile());
        view.setReportAction(e -> exportReport());
    }

    private void loadInventory() {
        final String searchText = view.getSearchText();
        new javax.swing.SwingWorker<java.util.List<InventoryItem>, Void>() {
            @Override
            protected java.util.List<InventoryItem> doInBackground() throws Exception {
                return service.findAll(searchText);
            }

            @Override
            protected void done() {
                try {
                    view.renderItems(get());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(view, "Error cargando inventario: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void importFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Inventario CSV/XLSX", "csv", "xlsx"));
        int result = chooser.showOpenDialog(view);
        if (result != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        try {
            InventoryService.ImportPreview preview = service.previewImport(file);
            if (!preview.errors().isEmpty()) {
                JOptionPane.showMessageDialog(view, "Errores detectados:\n" + String.join("\n", preview.errors()));
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(view,
                    "Se importarán " + preview.validRows().size() + " filas válidas.\n¿Deseas continuar?",
                    "Preview importación", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                service.importRows(preview.validRows());
                loadInventory();
                SalesServiceCatalog.getInstance().reload();
                JOptionPane.showMessageDialog(view, "Inventario importado correctamente.");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(view, "No se pudo importar: " + ex.getMessage());
        }
    }

    private void exportReport() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Guardar reporte de inventario");
        chooser.setSelectedFile(new File("reporte_inventario.csv"));
        int result = chooser.showSaveDialog(view);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".csv")) {
            file = new File(file.getParentFile(), file.getName() + ".csv");
        }
        try {
            service.exportCsv(file.toPath(), view.getSearchText());
            JOptionPane.showMessageDialog(view, "Reporte generado:\n" + file.getAbsolutePath());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(view, "No se pudo generar reporte: " + ex.getMessage());
        }
    }

    public void addDemoItem() {
        try {
            service.upsert(new InventoryItem(0, 1, 1, "SKU-DEMO", "Producto Demo", "General", 10, 3, "Proveedor", 1000, 1500, "ACTIVO"));
            loadInventory();
        } catch (SQLException ignored) { }
    }
}
