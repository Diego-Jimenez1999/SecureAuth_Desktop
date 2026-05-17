package secureauth.controller;

import java.io.File;
import java.sql.SQLException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import secureauth.events.AppEventBus;
import secureauth.events.InventoryUpdatedEvent;
import secureauth.model.enterprise.InventoryItem;
import secureauth.service.enterprise.InventoryService;
import secureauth.ui.components.PanelInventory;
import secureauth.ui.dialogs.InventoryImportWizardDialog;

/** Controlador enterprise del inventario con importación CSV/XLSX. */
public class InventoryController {

    private final PanelInventory view;
    private final InventoryService service;

    public InventoryController(PanelInventory view) {
        this.view = view;
        this.service = new InventoryService();
        bindActions();
        initialize();
    }

    private void initialize() {
        try {
            service.initializeSchema();
            loadInventory();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(view, "No se pudo inicializar inventario: " + ex.getMessage());
        }
    }

    private void bindActions() {
        view.setSearchAction(e -> loadInventory());
        view.setImportAction(e -> importFile());
    }

    private void loadInventory() {
        try {
            view.renderItems(service.findAll(view.getSearchText()));
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(view, "Error cargando inventario: " + ex.getMessage());
        }
    }

    private void importFile() {
        JFileChooser chooser = new JFileChooser();
        int result = chooser.showOpenDialog(view);
        if (result != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        try {
            java.awt.Window w = javax.swing.SwingUtilities.getWindowAncestor(view);
            javax.swing.JFrame frame = w instanceof javax.swing.JFrame ? (javax.swing.JFrame) w : null;
            InventoryImportWizardDialog dialog = new InventoryImportWizardDialog(frame, service, file);
            dialog.setVisible(true);
            loadInventory();
            AppEventBus.getInstance().publish(new InventoryUpdatedEvent("import"));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(view, "No se pudo importar: " + ex.getMessage());
        }
    }

    public void addDemoItem() {
        try {
            service.upsert(new InventoryItem(0, 1, 1, "SKU-DEMO", "Producto Demo", "General", 10, 3, "Proveedor", 1000, 1500, "ACTIVO"));
            loadInventory();
            AppEventBus.getInstance().publish(new InventoryUpdatedEvent("manual"));
        } catch (SQLException ignored) { }
    }
}
