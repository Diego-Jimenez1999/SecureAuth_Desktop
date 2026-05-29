package secureauth.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.DefaultListModel;

import secureauth.model.SaleItem;

/**
 * Controlador del módulo de ventas.
 *
 * <p>Gestiona el carrito en memoria, acumula cantidades por producto/servicio y
 * recalcula subtotal, IVA y total cada vez que cambia una cantidad.</p>
 *
 * <h2>Ejemplo</h2>
 * <pre>{@code
 * SalesController controller = new SalesController();
 * controller.addItem(new SaleItem("Baño Canino", 35000));
 * controller.updateQuantity(0, 2);
 * double total = controller.getTotal();
 * }</pre>
 */
public class SalesController {

    private static final double TAX_RATE = 0.19;

    private final List<SaleItem> saleItems;
    private final DefaultListModel<String> listModel;

    /**
     * Inicializa el controlador con estado vacío.
     */
    public SalesController() {
        this.saleItems = new ArrayList<>();
        this.listModel = new DefaultListModel<>();
    }

    /**
     * Agrega un producto o servicio al carrito. Si ya existe una línea
     * compatible, incrementa la cantidad para mantener una sola fila por item.
     *
     * @param item item seleccionado desde el catálogo
     */
    public void addItem(SaleItem item) {
        int existingIndex = findMatchingItemIndex(item);
        if (existingIndex >= 0) {
            saleItems.get(existingIndex).incrementQuantity();
        } else {
            saleItems.add(item);
        }
        syncListModel();
    }

    /**
     * Elimina un item por índice del carrito actual.
     *
     * @param index fila seleccionada en la lista de venta
     * @return true si se eliminó un item
     */
    public boolean removeItemAt(int index) {
        if (index < 0 || index >= saleItems.size()) {
            return false;
        }
        saleItems.remove(index);
        syncListModel();
        return true;
    }

    /**
     * Incrementa la cantidad de una línea del carrito.
     *
     * @param index fila del carrito
     * @return true si la fila existe y fue actualizada
     */
    public boolean incrementQuantity(int index) {
        if (!isValidIndex(index)) {
            return false;
        }
        saleItems.get(index).incrementQuantity();
        syncListModel();
        return true;
    }

    /**
     * Disminuye la cantidad de una línea del carrito.
     *
     * @param index fila del carrito
     * @return true si la fila existe y fue actualizada
     */
    public boolean decrementQuantity(int index) {
        if (!isValidIndex(index)) {
            return false;
        }
        saleItems.get(index).decrementQuantity();
        syncListModel();
        return true;
    }

    /**
     * Define manualmente la cantidad de una línea del carrito.
     *
     * @param index fila del carrito
     * @param quantity cantidad mayor a cero
     * @return true si la fila existe y fue actualizada
     */
    public boolean updateQuantity(int index, int quantity) {
        if (!isValidIndex(index)) {
            return false;
        }
        saleItems.get(index).setQuantity(quantity);
        syncListModel();
        return true;
    }

    /**
     * @return modelo para pintar la lista en la vista
     */
    public DefaultListModel<String> getListModel() {
        return listModel;
    }

    /**
     * @return subtotal (sin impuestos)
     */
    public double getSubtotal() {
        return saleItems.stream().mapToDouble(SaleItem::getSubtotal).sum();
    }

    /**
     * @return valor del IVA al 19%
     */
    public double getTax() {
        return getSubtotal() * TAX_RATE;
    }

    /**
     * @return total final incluyendo IVA
     */
    public double getTotal() {
        return getSubtotal() + getTax();
    }

    /**
     * @return vista inmutable de items agregados
     */
    public List<SaleItem> getItems() {
        return Collections.unmodifiableList(saleItems);
    }

    /**
     * Limpia el estado de la venta actual.
     */
    public void clearSale() {
        saleItems.clear();
        listModel.clear();
    }

    /**
     * @return cantidad total de unidades vendidas
     */
    public int getItemsCount() {
        return saleItems.stream().mapToInt(SaleItem::getQuantity).sum();
    }

    private int findMatchingItemIndex(SaleItem item) {
        for (int i = 0; i < saleItems.size(); i++) {
            SaleItem current = saleItems.get(i);
            boolean sameInventory = current.getInventoryItemId() != null
                    && current.getInventoryItemId().equals(item.getInventoryItemId());
            boolean sameCatalog = current.getInventoryItemId() == null
                    && current.getCatalogItemId() == item.getCatalogItemId()
                    && current.getName().equals(item.getName())
                    && Double.compare(current.getPrice(), item.getPrice()) == 0;
            if (sameInventory || sameCatalog) {
                return i;
            }
        }
        return -1;
    }

    private boolean isValidIndex(int index) {
        return index >= 0 && index < saleItems.size();
    }

    private void syncListModel() {
        listModel.clear();
        for (SaleItem item : saleItems) {
            listModel.addElement(item.toString());
        }
    }
}
