package secureauth.application.dto;

import java.util.List;

import secureauth.domain.services.ServiceOrderStatus;
import secureauth.domain.services.ServiceSummary;

public record ServiceOrderDTO(
        Integer id,
        Integer customerId,
        String customerName,
        Integer petId,
        String petName,
        ServiceOrderStatus status,
        ServiceOrderItemDTO item,
        List<ServiceProductDTO> products,
        List<ServiceProductDTO> suggestedProducts,
        ServiceSummary summary) {

    public ServiceOrderDTO {
        products = List.copyOf(products == null ? List.of() : products);
        suggestedProducts = List.copyOf(suggestedProducts == null ? List.of() : suggestedProducts);
        status = status == null ? ServiceOrderStatus.DRAFT : status;
    }
}
