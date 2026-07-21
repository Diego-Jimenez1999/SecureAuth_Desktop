package secureauth.application.usecase;

import java.util.List;

import secureauth.application.dto.ServiceOrderDTO;
import secureauth.application.dto.ServiceOrderItemDTO;
import secureauth.application.dto.ServiceProductDTO;
import secureauth.domain.services.ServiceOrderStatus;
import secureauth.domain.services.ServiceSummary;

public class CreateServiceOrderUseCase {

    private final ValidateServiceOrderUseCase validator;

    public CreateServiceOrderUseCase() {
        this(new ValidateServiceOrderUseCase());
    }

    public CreateServiceOrderUseCase(ValidateServiceOrderUseCase validator) {
        this.validator = validator;
    }

    public ServiceOrderDTO create(Integer customerId, String customerName, Integer petId, String petName,
            ServiceOrderItemDTO item, List<ServiceProductDTO> products, List<ServiceProductDTO> suggestedProducts,
            double discount) {
        if (item == null) {
            ServiceOrderDTO invalidOrder = new ServiceOrderDTO(null, customerId, customerName, petId, petName,
                    ServiceOrderStatus.SCHEDULED, null, products, suggestedProducts, null);
            validator.validate(invalidOrder);
        }
        ServiceOrderDTO order = new ServiceOrderDTO(null, customerId, customerName, petId, petName,
                ServiceOrderStatus.SCHEDULED, item, products, suggestedProducts,
                ServiceSummary.calculate(item.servicePrice(), toDomainProducts(products), discount));
        validator.validate(order);
        return order;
    }

    private List<secureauth.domain.services.ServiceProduct> toDomainProducts(List<ServiceProductDTO> products) {
        return products == null ? List.of() : products.stream()
                .map(product -> new secureauth.domain.services.ServiceProduct(product.productId(), product.sku(),
                        product.name(), product.quantity(), product.unitPrice()))
                .toList();
    }
}
