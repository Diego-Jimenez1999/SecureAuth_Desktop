package secureauth.application.usecase;

import java.util.List;

import secureauth.application.dto.ServiceOrderDTO;
import secureauth.application.dto.ServiceProductDTO;
import secureauth.domain.services.ServiceProduct;
import secureauth.domain.services.ServiceSummary;

public class UpdateServiceOrderUseCase {

    private final ValidateServiceOrderUseCase validator;

    public UpdateServiceOrderUseCase() {
        this(new ValidateServiceOrderUseCase());
    }

    public UpdateServiceOrderUseCase(ValidateServiceOrderUseCase validator) {
        this.validator = validator;
    }

    public ServiceOrderDTO updateProducts(ServiceOrderDTO order, List<ServiceProductDTO> products, double discount) {
        validator.validate(order);
        List<ServiceProductDTO> normalizedProducts = products == null ? List.of() : products;
        ServiceOrderDTO updated = new ServiceOrderDTO(order.id(), order.customerId(), order.customerName(),
                order.petId(), order.petName(), order.status(), order.item(), normalizedProducts,
                order.suggestedProducts(),
                ServiceSummary.calculate(order.item().servicePrice(), toDomainProducts(normalizedProducts), discount));
        validator.validate(updated);
        return updated;
    }

    private List<ServiceProduct> toDomainProducts(List<ServiceProductDTO> products) {
        return products.stream()
                .map(product -> new ServiceProduct(product.productId(), product.sku(), product.name(),
                        product.quantity(), product.unitPrice()))
                .toList();
    }
}
