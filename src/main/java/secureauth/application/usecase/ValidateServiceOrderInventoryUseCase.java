package secureauth.application.usecase;

import java.util.List;

import secureauth.application.dto.ServiceOrderDTO;
import secureauth.application.dto.ServiceProductDTO;

public class ValidateServiceOrderInventoryUseCase {

    public void validate(List<ServiceOrderDTO> orders) {
        if (orders == null) {
            return;
        }
        for (ServiceOrderDTO order : orders) {
            if (order == null) {
                throw new IllegalArgumentException("La orden de servicio no puede ser nula.");
            }
            for (ServiceProductDTO product : order.products()) {
                if (product.productId() == null || product.productId() <= 0) {
                    throw new IllegalArgumentException("Producto de inventario inválido en orden de servicio.");
                }
                if (product.quantity() <= 0) {
                    throw new IllegalArgumentException("La cantidad consumida debe ser mayor que cero.");
                }
            }
        }
    }
}
