package secureauth.application.mapper;

import secureauth.application.dto.ServiceOrderDTO;
import secureauth.application.dto.ServiceOrderItemDTO;
import secureauth.application.dto.ServiceProductDTO;
import secureauth.domain.services.ServiceOrder;
import secureauth.domain.services.ServiceOrderItem;
import secureauth.domain.services.ServiceProduct;

public final class ServiceOrderMapper {

    private ServiceOrderMapper() {
    }

    public static ServiceProduct toDomain(ServiceProductDTO dto) {
        return new ServiceProduct(dto.productId(), dto.sku(), dto.name(), dto.quantity(), dto.unitPrice());
    }

    public static ServiceProductDTO toDTO(ServiceProduct product) {
        return new ServiceProductDTO(product.productId(), product.sku(), product.name(), product.quantity(),
                product.unitPrice());
    }

    public static ServiceOrderItem toDomain(ServiceOrderItemDTO dto) {
        return new ServiceOrderItem(dto.serviceId(), dto.serviceName(), dto.veterinarian(), dto.serviceDate(),
                dto.serviceTime(), dto.durationMinutes(), dto.observations(), dto.servicePrice());
    }

    public static ServiceOrderItemDTO toDTO(ServiceOrderItem item) {
        return new ServiceOrderItemDTO(item.serviceId(), item.serviceName(), item.veterinarian(), item.serviceDate(),
                item.serviceTime(), item.durationMinutes(), item.observations(), item.servicePrice());
    }

    public static ServiceOrder toDomain(ServiceOrderDTO dto) {
        return new ServiceOrder(dto.id(), dto.customerId(), dto.customerName(), dto.petId(), dto.petName(),
                dto.status(), toDomain(dto.item()), dto.products().stream().map(ServiceOrderMapper::toDomain).toList(),
                dto.suggestedProducts().stream().map(ServiceOrderMapper::toDomain).toList(), dto.summary());
    }

    public static ServiceOrderDTO toDTO(ServiceOrder order) {
        return new ServiceOrderDTO(order.id(), order.customerId(), order.customerName(), order.petId(),
                order.petName(), order.status(), toDTO(order.item()),
                order.products().stream().map(ServiceOrderMapper::toDTO).toList(),
                order.suggestedProducts().stream().map(ServiceOrderMapper::toDTO).toList(), order.summary());
    }
}
