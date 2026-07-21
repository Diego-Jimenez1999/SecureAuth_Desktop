package secureauth.application.usecase;

import java.util.HashSet;
import java.util.Set;

import secureauth.application.dto.ServiceOrderDTO;
import secureauth.application.dto.ServiceProductDTO;

public class ValidateServiceOrderUseCase {

    public void validate(ServiceOrderDTO order) {
        if (order == null) {
            throw new IllegalArgumentException("La orden de servicio no puede ser nula.");
        }
        if (order.customerId() == null || order.customerId() <= 0 || isBlank(order.customerName())) {
            throw new IllegalArgumentException("La orden de servicio debe tener cliente.");
        }
        if (order.petId() == null || order.petId() <= 0 || isBlank(order.petName())) {
            throw new IllegalArgumentException("La orden de servicio debe tener mascota.");
        }
        if (order.item() == null || order.item().serviceId() <= 0 || isBlank(order.item().serviceName())) {
            throw new IllegalArgumentException("La orden de servicio debe tener servicio.");
        }
        if (isBlank(order.item().veterinarian())) {
            throw new IllegalArgumentException("La orden de servicio debe tener veterinario.");
        }
        if (order.item().serviceDate() == null || order.item().serviceTime() == null) {
            throw new IllegalArgumentException("La orden de servicio debe tener fecha y hora.");
        }
        if (order.item().servicePrice() < 0d || order.summary() != null && order.summary().total() < 0d) {
            throw new IllegalArgumentException("La orden de servicio no puede tener totales negativos.");
        }
        validateProducts(order.products());
    }

    private void validateProducts(java.util.List<ServiceProductDTO> products) {
        Set<Integer> productIds = new HashSet<>();
        Set<String> names = new HashSet<>();
        for (ServiceProductDTO product : products) {
            if (product == null || isBlank(product.name())) {
                throw new IllegalArgumentException("La orden contiene productos incompletos.");
            }
            if (product.quantity() <= 0) {
                throw new IllegalArgumentException("La cantidad del producto debe ser mayor que cero.");
            }
            if (product.unitPrice() < 0d) {
                throw new IllegalArgumentException("El precio del producto no puede ser negativo.");
            }
            if (product.productId() != null && !productIds.add(product.productId())) {
                throw new IllegalArgumentException("La orden contiene productos duplicados.");
            }
            if (product.productId() == null && !names.add(product.name().trim().toLowerCase())) {
                throw new IllegalArgumentException("La orden contiene productos duplicados.");
            }
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
