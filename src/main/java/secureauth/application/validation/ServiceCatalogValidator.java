package secureauth.application.validation;

import java.util.HashSet;
import java.util.Set;

import secureauth.application.dto.ServiceCatalogDTO;
import secureauth.application.dto.ServiceProductDTO;

public class ServiceCatalogValidator {

    public void validate(ServiceCatalogDTO service) {
        if (service == null) {
            throw new IllegalArgumentException("El servicio no puede ser nulo.");
        }
        if (isBlank(service.name())) {
            throw new IllegalArgumentException("El nombre del servicio es obligatorio.");
        }
        if (isBlank(service.code())) {
            throw new IllegalArgumentException("El código del servicio es obligatorio.");
        }
        if (isBlank(service.category())) {
            throw new IllegalArgumentException("La categoría del servicio es obligatoria.");
        }
        if (service.price() <= 0d) {
            throw new IllegalArgumentException("El precio del servicio debe ser mayor que cero.");
        }
        if (service.durationMinutes() <= 0) {
            throw new IllegalArgumentException("La duración del servicio debe ser mayor que cero.");
        }
        validateSuggestedProducts(service);
    }

    private void validateSuggestedProducts(ServiceCatalogDTO service) {
        Set<Integer> productIds = new HashSet<>();
        Set<String> names = new HashSet<>();
        for (ServiceProductDTO product : service.suggestedProducts()) {
            if (product == null || isBlank(product.name())) {
                throw new IllegalArgumentException("Hay productos sugeridos incompletos.");
            }
            if (product.quantity() <= 0) {
                throw new IllegalArgumentException("La cantidad sugerida debe ser mayor que cero.");
            }
            if (product.unitPrice() < 0d) {
                throw new IllegalArgumentException("El precio sugerido no puede ser negativo.");
            }
            if (product.productId() != null && !productIds.add(product.productId())) {
                throw new IllegalArgumentException("Hay productos sugeridos duplicados.");
            }
            if (product.productId() == null && !names.add(product.name().trim().toLowerCase())) {
                throw new IllegalArgumentException("Hay productos sugeridos duplicados.");
            }
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
