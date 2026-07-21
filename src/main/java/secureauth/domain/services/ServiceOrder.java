package secureauth.domain.services;

import java.util.List;

public record ServiceOrder(
        Integer id,
        Integer customerId,
        String customerName,
        Integer petId,
        String petName,
        ServiceOrderStatus status,
        ServiceOrderItem item,
        List<ServiceProduct> products,
        List<ServiceProduct> suggestedProducts,
        ServiceSummary summary) {

    public ServiceOrder {
        products = List.copyOf(products == null ? List.of() : products);
        suggestedProducts = List.copyOf(suggestedProducts == null ? List.of() : suggestedProducts);
        status = status == null ? ServiceOrderStatus.DRAFT : status;
    }

    public ServiceOrder withProducts(List<ServiceProduct> newProducts, double discount) {
        return new ServiceOrder(id, customerId, customerName, petId, petName, status, item, newProducts,
                suggestedProducts, ServiceSummary.calculate(item.servicePrice(), newProducts, discount));
    }
}
