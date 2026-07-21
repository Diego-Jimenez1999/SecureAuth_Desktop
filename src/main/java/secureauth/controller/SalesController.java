package secureauth.controller;

import java.util.Collections;
import java.util.List;

import javax.swing.DefaultListModel;

import secureauth.application.dto.SaleItemDTO;
import secureauth.application.mapper.SaleMapper;
import secureauth.application.usecase.SalesCartUseCase;
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

    private final SalesCartUseCase cartUseCase;
    private final DefaultListModel<String> listModel;

    /**
     * Inicializa el controlador con estado vacío.
     */
    public SalesController() {
        this.cartUseCase = new SalesCartUseCase();
        this.listModel = new DefaultListModel<>();
    }

    /**
     * Agrega un producto o servicio al carrito. Si ya existe una línea
     * compatible, incrementa la cantidad para mantener una sola fila por item.
     *
     * @param item item seleccionado desde el catálogo
     */
    public void addItem(SaleItem item) {
        addItem(SaleMapper.toDTO(item));
    }

    /**
     * Agrega una linea desacoplada del modelo legacy al carrito.
     *
     * @param item item seleccionado desde el catalogo
     */
    public void addItem(SaleItemDTO item) {
        cartUseCase.addItem(item);
        syncListModel();
    }

    /**
     * Elimina un item por índice del carrito actual.
     *
     * @param index fila seleccionada en la lista de venta
     * @return true si se eliminó un item
     */
    public boolean removeItemAt(int index) {
        boolean removed = cartUseCase.removeItemAt(index);
        syncListModel();
        return removed;
    }

    /**
     * Incrementa la cantidad de una línea del carrito.
     *
     * @param index fila del carrito
     * @return true si la fila existe y fue actualizada
     */
    public boolean incrementQuantity(int index) {
        boolean updated = cartUseCase.incrementQuantity(index);
        syncListModel();
        return updated;
    }

    /**
     * Disminuye la cantidad de una línea del carrito.
     *
     * @param index fila del carrito
     * @return true si la fila existe y fue actualizada
     */
    public boolean decrementQuantity(int index) {
        boolean updated = cartUseCase.decrementQuantity(index);
        syncListModel();
        return updated;
    }

    /**
     * Define manualmente la cantidad de una línea del carrito.
     *
     * @param index fila del carrito
     * @param quantity cantidad mayor a cero
     * @return true si la fila existe y fue actualizada
     */
    public boolean updateQuantity(int index, int quantity) {
        boolean updated = cartUseCase.updateQuantity(index, quantity);
        syncListModel();
        return updated;
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
        return cartUseCase.subtotal();
    }

    /**
     * @return valor del IVA al 19%
     */
    public double getTax() {
        return cartUseCase.tax();
    }

    /**
     * @return total final incluyendo IVA
     */
    public double getTotal() {
        return cartUseCase.total();
    }

    /**
     * @return vista inmutable de items agregados
     */
    public List<SaleItem> getItems() {
        return Collections.unmodifiableList(cartUseCase.getItems().stream().map(SaleMapper::toDomain).toList());
    }

    public List<SaleItemDTO> getItemDTOs() {
        return cartUseCase.getItems();
    }

    /**
     * Limpia el estado de la venta actual.
     */
    public void clearSale() {
        cartUseCase.clear();
        listModel.clear();
    }

    /**
     * @return cantidad total de unidades vendidas
     */
    public int getItemsCount() {
        return cartUseCase.unitsCount();
    }

    private void syncListModel() {
        listModel.clear();
        for (SaleItemDTO item : cartUseCase.getItems()) {
            listModel.addElement(SaleMapper.toDomain(item).toString());
        }
    }
}
