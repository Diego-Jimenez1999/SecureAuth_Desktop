package secureauth.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.DefaultListModel;

import secureauth.model.SaleItem;

/**
 * Controlador del módulo de ventas.
 *
 * <p>Gestiona la lógica de negocio de la venta en memoria:
 * agrega items al resumen y calcula subtotal, IVA y total.
 * </p>
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
     * Agrega un servicio al carrito/resumen.
     *
     * @param item servicio seleccionado desde el catálogo
     */
    public void addItem(SaleItem item) {
        saleItems.add(item);
        listModel.addElement(item.toString());
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
        return saleItems.stream().mapToDouble(SaleItem::getPrice).sum();
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
}
