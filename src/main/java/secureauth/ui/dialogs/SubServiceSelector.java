package secureauth.ui.dialogs;

import java.awt.Window;
import java.util.List;

import secureauth.model.SaleItem;

/**
 * Selector de sub-servicios desacoplado de la vista de ventas.
 */
@FunctionalInterface
public interface SubServiceSelector {

    /**
     * Abre un selector de sub-servicios y retorna el item elegido.
     *
     * @param parent ventana padre
     * @param serviceName servicio principal
     * @param subServices sub-servicios disponibles
     * @return sub-servicio elegido o {@code null} si se cancela
     */
    SaleItem select(Window parent, String serviceName, List<SaleItem> subServices);
}
