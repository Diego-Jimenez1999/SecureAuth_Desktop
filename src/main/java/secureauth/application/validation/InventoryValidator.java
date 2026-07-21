package secureauth.application.validation;

import secureauth.application.dto.SaleItemDTO;

public class InventoryValidator {

    public void validateStock(SaleItemDTO item) {
        if (item != null && item.stockAvailable() != null && item.quantity() > item.stockAvailable()) {
            throw new IllegalArgumentException("No hay suficiente inventario disponible.");
        }
    }
}
