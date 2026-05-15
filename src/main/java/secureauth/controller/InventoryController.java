package secureauth.controller;

import javax.swing.JOptionPane;

import secureauth.ui.components.PanelInventory;

/**
 * Controlador inicial del módulo de inventario.
 */
public class InventoryController {

    private final PanelInventory view;

    public InventoryController(PanelInventory view) {
        this.view = view;
        bindActions();
    }

    private void bindActions() {
        view.setSearchAction(e -> onSearch());
        view.setNewProductAction(e -> onNewProduct());
    }

    private void onSearch() {
        String query = view.getSearchText();
        // TODO: reemplazar por consulta DAO/Service real del inventario.
        JOptionPane.showMessageDialog(view, "Buscar inventario: " + query);
    }

    private void onNewProduct() {
        // TODO: abrir formulario real de creación de producto.
        JOptionPane.showMessageDialog(view, "Abrir formulario: Nuevo Producto");
    }
}
